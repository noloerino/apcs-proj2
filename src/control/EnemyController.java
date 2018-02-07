package control;

import characterbuilder.GameCharacter;
import core.Alignment;
import core.IntVector2D;
import ui.MapPanel;
import ui.MapViewState;
import worldmap.MapConfiguration;
import worldmap.Pathfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class EnemyController implements DS_Actionable {
	
	private MapConfiguration config;
	
	private Iterator<IntVector2D> iter;
	
	public static DebugLogger log = new DebugLogger("EnemyController");
	
	/**
	 * Runs an enemy turn.
	 * The AI is very predictable, and will make moves according to the following heuristic:
	 * 1) Attack any enemy units in range.
	 *     a) If there are multiple attackable enemy units, then attack the one that would take
	 *        the fewest hits to kill, generally the one with the smallest ratio of remaining HP
	 *        to potential damage dealt.
	 *     b) If the enemies in attackable range have the same such ratio, then attack the last
	 *        such unit.
	 * 2) If there are no attackable units in range, find the enemy unit on the map that meets
	 *    the criteria specified in directive (1). Move to the nearest possible square to that
	 *    enemy.
	 *     a) If more than one square is at the shortest distance possible, then choose the first
	 *        one detected.
	 * 
	 * @param config
	 */
	public EnemyController(MapConfiguration config) {
		this.config = config;
	}
	
	private void runForEnemy(MapPanel caller, Pathfinder pf, IntVector2D v) {
		log.log("Running turn for enemy at " + v);
		GameCharacter g = config.getUnitAt(v).getEnclosed();
		List<IntVector2D> rangeAtk = config.findUnitsWithinRangedRange(v);
		List<IntVector2D> meleeAtk = config.findUnitsWithinMeleeRange(v);
		List<IntVector2D> movable = config.findMovableTo(v);
		List<IntVector2D> friendlyCoords = new ArrayList<>(config.getFriendlyCoords());
		
		Map<IntVector2D, Integer> outcomeMaps = new HashMap<>();
		Map<IntVector2D, Double> dmgMaps = new HashMap<>();
		simulateM(g, meleeAtk, outcomeMaps, dmgMaps);
		simulateR(g, rangeAtk, outcomeMaps, dmgMaps);
		// First check: combat outcomes
		List<IntVector2D> winningOutcomes = outcomeMaps.keySet().stream()
				.filter(pos -> outcomeMaps.get(pos) == DEFENDER_DEATH)
				.collect(Collectors.toList());
		if(winningOutcomes.size() != 0) {
			log.log("Enemy at " + v + " found a kill within range...");
			IntVector2D atkDest = winningOutcomes.get(0);
			// I am aware that if the thing can be attacked both melee and range and one allows
			// the enemy to counterattack, then this algorithm is wrong but w/e
			IntVector2D mvDest = findGC(movable, atkDest, rangeAtk.contains(atkDest));
			log.log("Enemy at " + v + " detected easy attack at " + atkDest + ", attempting move to " + mvDest);
			config.setActivePath(pf.getFirstPath(v, mvDest));
			makeMoveCall(caller, atkDest);
			
			return;
		}
		// Second check: damage outcomes
		if(dmgMaps.size() != 0) {
			log.log("Enemy at " + v + " is searching for highest damage ratio...");
			Iterator<IntVector2D> iter = dmgMaps.keySet().iterator();
			try {
				IntVector2D vWithMaxDmgRatio = iter.next();
				double maxRatio = dmgMaps.get(vWithMaxDmgRatio);
				IntVector2D run;
				double rund;
				while(iter.hasNext()) {
					run = iter.next();
					rund = dmgMaps.get(run);
					if(rund > maxRatio) {
						maxRatio = rund;
						vWithMaxDmgRatio = run;
					}
				}
				IntVector2D mvDest = findGC(movable, vWithMaxDmgRatio, rangeAtk.contains(vWithMaxDmgRatio));
				log.log("Enemy at " + v + " detected max ratio attack at  " + vWithMaxDmgRatio + ", attempting move to " + mvDest);
				config.setActivePath(pf.getFirstPath(v, mvDest));
				makeMoveCall(caller, vWithMaxDmgRatio);
				return;
			}
			catch(NoSuchElementException e) { }
		}
		// Third check: moving to target
		IntVector2D weakest = findWeakest(g, friendlyCoords);
		log.log("Enemy at " + v + " is searching for the weakest enemy...");
		// (find closest square)
		try {
			IntVector2D closest = v;
			for(IntVector2D pos : movable) {
				if(pos.getDistanceFrom(weakest) < closest.getDistanceFrom(weakest))
					closest = pos;
			}
			config.setActivePath(pf.getFirstPath(v, closest));
			makeMoveCall(caller);
			log.log("Enemy at " + v + " moved towards " + weakest + ", landing on " + closest);
			return;
		}
		catch(NullPointerException e) { }
		// If all else fails, just stay lol
		config.setActivePath(pf.getFirstPath(v, v));
		makeMoveCall(caller);
	}
	
	private void makeMoveCall(MapPanel caller) {
		makeMoveCall(caller, null);
	}
	
	private void makeMoveCall(MapPanel caller, IntVector2D dest) {
		MapViewState vs = caller.getMapViewState();
		vs.setExamined(caller, caller.getMapConfig().getActivePath().get(0));
		if(dest != null)
			vs.setSelected(caller, dest);
		caller.getMapConfig().makeMove(caller, dest);
	}
	
	// remember that makemove now will automatically end turn
	public void startTurn(MapPanel caller) {
		if(config.getSideToMove() != Alignment.ENEMY)
			throw new IllegalArgumentException("Attempted to run enemy controller on player turn.");
		List<IntVector2D> enemyCoords = new ArrayList<>(config.getEnemyCoords());
		iter = enemyCoords.iterator();
	}
	
	public void next(MapPanel caller, Pathfinder pf) {
		if(iter.hasNext())
			runForEnemy(caller, pf, iter.next());
	}
	
	private IntVector2D findWeakest(GameCharacter g, List<IntVector2D> friendlies) {
		IntVector2D v = friendlies.get(0);
		Map<IntVector2D, Integer> ocMap = new HashMap<>();
		Map<IntVector2D, Double> dmgMap = new HashMap<>();
		simulateR(g, friendlies, ocMap, dmgMap);
		simulateM(g, friendlies, ocMap, dmgMap);
		double dmg = dmgMap.get(v);
		for(Map.Entry<IntVector2D, Double> kv : dmgMap.entrySet()) {
			if(kv.getValue() > dmg) {
				dmg = kv.getValue();
				v = kv.getKey();
			}
		}
		return v;
	}
	
	private IntVector2D findGC(List<IntVector2D> movables, IntVector2D dest, boolean ranged) {
		int dist = ranged ? 2 : 1;
		int x = dest.getX();
		int y = dest.getY();
		IntVector2D v = new IntVector2D(x + dist, y);
		if(movables.contains(v))
			return v;
		v.set(x - dist, y);
		if(movables.contains(v))
			return v;
		v.set(x, y + dist);
		if(movables.contains(v))
			return v;
		v.set(x, y - dist);
		if(movables.contains(v))
			return v;
		return null;
	}
	
	private void simulateM(GameCharacter g, List<IntVector2D> atkble, Map<IntVector2D, Integer> ocMap,
			Map<IntVector2D, Double> dmgMap) {
		for(IntVector2D v : atkble) {
			initiateFight(g, v, ocMap,dmgMap, false);
		}
	}
	
	private void simulateR(GameCharacter g, List<IntVector2D> atkble, Map<IntVector2D, Integer> ocMap,
			Map<IntVector2D, Double> dmgMap) {
		for(IntVector2D v : atkble) {
			initiateFight(g, v, ocMap, dmgMap, true);
		}
	}
	
	private final static int NO_DEATHS = GameCharacter.NO_DEATHS;
	private final static int ATTACKER_DEATH = GameCharacter.ATTACKER_DEATH;
	private final static int DEFENDER_DEATH = GameCharacter.DEFENDER_DEATH;
	
	private void initiateFight(GameCharacter g, IntVector2D oppPos, Map<IntVector2D, Integer> ocMap,
			Map<IntVector2D, Double> dmgMap, boolean ranged) {
		GameCharacter opp = config.getUnitAt(oppPos).getEnclosed();
		double dmgRatio = takeDmg(opp, g.getEffectiveAtk(), g.isWeaponPhys());
		dmgMap.put(oppPos, dmgRatio);
		if(dmgRatio > 1)
			ocMap.put(oppPos, DEFENDER_DEATH);
		else
			ocMap.put(oppPos, reactFight(g, oppPos, dmgMap, ranged));
	}
	
	/**
	 * If the returned result is negative, that means enemy doesn't take damage.
	 * 
	 * @param c
	 * @param atk
	 * @param physical
	 * @return
	 */
	private double takeDmg(GameCharacter c, int atk, boolean physical) {
		if(physical)
			atk -= c.getDef();
		else
			atk -= c.getRes();
		if(atk < 0)
			atk = 0;
		return (double) atk / c.getHP();
	}
	
	private int reactFight(GameCharacter g, IntVector2D oppPos, Map<IntVector2D, Double> dmgMap,
			boolean ranged) {
		GameCharacter opp = config.getUnitAt(oppPos).getEnclosed();
		double dmgRatio = 0;
		if(ranged) {
			if(opp.isRanged()) {
				dmgRatio = takeDmg(g, opp.getEffectiveAtk(), opp.isWeaponPhys());
				if(dmgRatio > 1) {
					dmgMap.put(oppPos, (double) -100);
					return ATTACKER_DEATH;
				}
			}
		}
		else {
			if(opp.isMelee()) {
				dmgRatio = takeDmg(g, opp.getEffectiveAtk(), opp.isWeaponPhys());
				if(dmgRatio > 1) {
					dmgMap.put(oppPos, (double) -100);
					return ATTACKER_DEATH;
				}
			}
		}
		
		if(g.getEffectiveSpd() > opp.getEffectiveSpd()) {
			dmgRatio += takeDmg(opp, g.getEffectiveAtk(), g.isWeaponPhys());
			if(dmgMap.get(oppPos) != null)
				dmgMap.put(oppPos, dmgMap.get(oppPos) + dmgRatio);
			else
				dmgMap.put(oppPos, dmgRatio);
			if(dmgRatio > 1)
				return DEFENDER_DEATH;
		}
		else if(opp.getEffectiveSpd() < g.getEffectiveSpd()) {
			if(takeDmg(g, opp.getEffectiveAtk(), opp.isWeaponPhys()) > 1); {
				dmgMap.put(oppPos, (double) -100);
				return ATTACKER_DEATH;
			}
		}
		return NO_DEATHS;
	}

	@Override
	public void onUp(MapPanel caller) { }

	@Override
	public void onDown(MapPanel caller) { }

	@Override
	public void onLeft(MapPanel caller) { }

	@Override
	public void onRight(MapPanel caller) { }

	@Override
	public void onCancel(MapPanel caller) { }

	@Override
	public void onConfirm(MapPanel caller) { }

	@Override
	public void onSelect(MapPanel caller) { }

	@Override
	public void onStart(MapPanel caller) { }

	@Override
	public void onLTrigger(MapPanel caller) { }

	@Override
	public void onRTrigger(MapPanel caller) { }
	
}
