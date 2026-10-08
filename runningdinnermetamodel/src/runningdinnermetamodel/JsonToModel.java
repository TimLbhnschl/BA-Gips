package runningdinnermetamodel;

public class JsonToModel {
	
	Root model = null;
	
	public void convertJsonToModel(final String path) {
		this.model = RunningdinnermetamodelFactory.eINSTANCE.createRoot();
		
		
		final Group g = RunningdinnermetamodelFactory.eINSTANCE.createGroup();
		g.setName("test");
		g.setSize(3);
		
		model.getGroups().add(g);
		
		model.getGroups();
		
	}

}
