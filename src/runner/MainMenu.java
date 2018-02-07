package runner;

import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MainMenu extends JPanel {

	private String savesDir;
	
	public List<String> lvlOrder;
	
	private JPanel buttonHolder;
	private LoadBTListener btl;

	public MainMenu(String dir) {
		lvlOrder = new ArrayList<String>();
		savesDir = dir;
		String line = "";
		try(BufferedReader br = new BufferedReader(new FileReader(savesDir + "ORDER.txt"))) {
			while(line != null) {
				if(line.length() != 0)
					lvlOrder.add(line);
				line = br.readLine();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		this.setLayout(new GridLayout(1, 2));
		this.add(new NewSaveHolder());
		buttonHolder = new LoadingButtonHolder(getSaves());
		this.add(buttonHolder);
	}
	
	private class NewSaveHolder extends JPanel {
		
		private NewSaveHolder() {
			this.add(new JLabel("Name for new save: "));
			JButton newGame = new JButton("New game");
			this.add(newGame);
			JTextField field = new JTextField(10);
			newGame.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					propagate("Tutorial", field.getText());
				}
				
			});
			this.add(field);
		}
		
	}
	
	public void propagate(String level, String save) {
		if(save.length() == 0)
			save = Arrays.stream(new String[8]).reduce("save",
					(acc, x) -> acc + Integer.toString((int) (Math.random() * 10)));
		MapWrapper.run(level, save);
	}
	
	public List<String> getSaves() {
		List<String> saves = new ArrayList<>();
		File[] files = new File(savesDir).listFiles();
		for(File f : files) {
			if(f.isDirectory())
				saves.add(f.getName());
		}
		return saves;
	}
	
	public void refresh() {
		this.remove(buttonHolder);
		buttonHolder = new LoadingButtonHolder(getSaves());
		this.add(buttonHolder);
	}
	
	// Doesn't check if save exists
	public void propagate(String save) {
		File progressFile = new File(savesDir + "/" + save + "/progress.txt");
		try(BufferedReader br = new BufferedReader(new FileReader(progressFile))) {
			String line = br.readLine();
			int idx = lvlOrder.indexOf(line) + 1;
			if(idx >= lvlOrder.size())
				idx = lvlOrder.size() - 1;
			propagate(lvlOrder.get(idx), save);
		}
		catch(IOException e) { }
	}
	
	
	private class LoadingButtonHolder extends JPanel {
		
		private LoadingButtonHolder(List<String> saves) {
			if(saves.size() == 0)
				return;
			this.add(new JLabel("Or load a save from this list:"));
			btl = new LoadBTListener();
			System.out.println("Found the following saves:\n___");
			for(int i = 0; i < saves.size(); i++) {
				System.out.println(saves.get(i));
				JButton button = new JButton(saves.get(i));
				button.addActionListener(btl);
				this.add(button);
			}
			System.out.println("___");
		}
		
	}
	
	private class LoadBTListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String path = e.getActionCommand();
			propagate(path);
		}
		
	}
	
}
