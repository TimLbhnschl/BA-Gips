package runningdinnermetamodel;

public class CsvToModel {
	
	Root model = null;
	
	public void convertCsvToModel(final String path) {
		this.model = RunningdinnermetamodelFactory.eINSTANCE.createRoot();
		
		//File reader
		
		for(int i = 1; i >0; i++) {
			final Team t = RunningdinnermetamodelFactory.eINSTANCE.createTeam();
			t.setEatingHabit(null);
			t.setEnvironment(null);
			t.setEnvironmentalAllergies(null);
			t.setLanguage(null);
			t.setName(null);
			t.setFoodAllergies(null);
			//t.setGroup(null);
			//t.setHosts(null);
			//t.setIsGuestAt(null);
		}
		
		final Group g = RunningdinnermetamodelFactory.eINSTANCE.createGroup();
		g.setName("test");
		g.setSize(3);
		
		model.getGroups().add(g);
		
		model.getGroups();
		
	}

}
