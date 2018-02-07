package characterbuilder;

public abstract class Skill {

	private String skillDescription;
	private int rank;
	
	
	public Skill(String skillDescription, int rank) {
		this.skillDescription = skillDescription;
		this.rank = rank;
	}
	
	public abstract void apply();
	
}
