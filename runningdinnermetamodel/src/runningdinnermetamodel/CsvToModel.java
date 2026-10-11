package runningdinnermetamodel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class CsvToModel {
	
	Root model = null;
	
	public void convertCsvToModel(final String pathTeams, final String pathDistanceMatrix) {
		try {
			
			this.model = RunningdinnermetamodelFactory.eINSTANCE.createRoot();
			
			//reading csv
	        final List<String> lines = new ArrayList<>();
	        for (String l : Files.readAllLines(Paths.get(pathTeams), StandardCharsets.UTF_8)) {
	            if (!l.isBlank()) lines.add(l);
	        }
			
	        final String[] header = lines.get(0).split(";", -1);
	        final Map<String, Integer> mapping = new HashMap<>();
	        for (int i = 0; i < header.length; i++) {
	            mapping.put(header[i].trim(), i);
	        }
	        
	        //Number of teams
	        final int size = lines.size() - 1;

	        if(size % 3 != 0) {
	        		throw new IOException("Team Anzahl muss durch 3 teilbar sein!");
	        }
			//Number of groups with size 12
			int size12 = (size % 9)/3;
			//Number of groups with size 9
			int size9 = (size - size12 * 12) / 9;

			if (size9 < 0) {
			    throw new IOException("Teamanzahl " + size + " lässt sich nicht in 9er/12er-Gruppen aufteilen!");
			}
			
			for(int i = 1; i <= size; i++) {
	            final String[] team = lines.get(i).split(";", -1);
	            //Name;Language;EatingHabit;Environment;EnvironmentalAllergies;FoodAllergies;KitchenPosition
				final Course c = RunningdinnermetamodelFactory.eINSTANCE.createCourse();
				String kitchenPos = get(team, mapping, "KitchenPosition");
				c.setKitchenPosition(kitchenPos);
				
				final Team t = RunningdinnermetamodelFactory.eINSTANCE.createTeam();
				//String name = get(team, mapping, "Name");
				t.setName(i);

				String language = get(team, mapping, "Language");
				t.setLanguage(parseEnum(Language.class, language));

				String eatingHabit = get(team, mapping, "EatingHabit");
				t.setEatingHabit(parseEnum(EatingHabit.class, eatingHabit));
				
				String environment = get(team, mapping, "Environment");
				if (!environment.isBlank()) {
					t.setEnvironment(parseEnum(CriticalAllergies.class, environment));
					//t.getEnvironment().add(parseEnum(CriticalAllergies.class, environment));					
				}

				String environmentalAllergies = get(team, mapping, "EnvironmentalAllergies");
				if (!environmentalAllergies.isBlank()) {					
					t.setEnvironmentalAllergies(parseEnum(CriticalAllergies.class, environmentalAllergies));
					//t.getEnvironmentalAllergies().add(parseEnum(CriticalAllergies.class, environmentalAllergies));
				}

				String foodAllergies = get(team, mapping, "FoodAllergies");
				if (!foodAllergies.isBlank()) {
					t.setFoodAllergies(foodAllergies);
					//t.getFoodAllergies().add(foodAllergies);					
				}
				
				//t.setGroup(null);
				//t.setIsGuestAt(null);
				
				t.setHosts(c);
				model.getTeams().add(t);
				model.getCourse().add(c);
				
			}
			
			for(int i = 1; i <= size9; i++) {
				final Group g = RunningdinnermetamodelFactory.eINSTANCE.createGroup();
				String name = "group9_" + i;
				g.setName(name);
				g.setSize(9);
				
				model.getGroups().add(g);
				
			}
			
			for(int i = 1; i <= size12; i++) {
				final Group g = RunningdinnermetamodelFactory.eINSTANCE.createGroup();
				String name = "group12_" + i;
				g.setName(name);
				g.setSize(12);
				
				model.getGroups().add(g);
				
			}
	        final List<String> linesDistance = new ArrayList<>();
	        for (String l : Files.readAllLines(Paths.get(pathDistanceMatrix), StandardCharsets.UTF_8)) {
	            if (!l.isBlank()) linesDistance.add(l.replace("\uFEFF", "").trim());
	        }
	        
	        int sizeDistance = linesDistance.size();
	        Double[][] distanceMatrix = new Double[sizeDistance][sizeDistance];
	        int x = 0;
			for(String cordsString : linesDistance) {
				String[] allCords = cordsString.split(",");
				int y = 0;
				for(String cord : allCords) {
					if(Double.parseDouble(cord) == Double.NaN) {
						distanceMatrix[x][y] = Double.MAX_VALUE;
					}
					else {
						distanceMatrix[x][y] = Double.parseDouble(cord);						
					}
					y++;
				}
				x++;
			}
			
			final Team[] teamsList = new Team[size];
			for(Team team : model.getTeams()) {
				int Index = team.getName()-1;
				teamsList[Index] = team;
			}
			
			for(int i = 0; i < sizeDistance; i++) {
				Team Team1 = teamsList[i];
				for(int j = 0; j < sizeDistance; j++) {
					if(i != j) {						
						Team Team2 = teamsList[j];
						Way way= RunningdinnermetamodelFactory.eINSTANCE.createWay();
						way.setDestination(Team2);
						way.setDistance(distanceMatrix[i][j]);
						Team1.getWay().add(way);
					}
				}	
			}	
		}
		catch (final Exception e) {
		    throw new RuntimeException("Fehler beim Aufbau des Modells", e);
		}
	}
	
	private static String get(String[] f, Map<String, Integer> col, String name) {
	    Integer index = col.get(name);
	    return (index == null || index >= f.length) ? "" : f[index].trim();
	}
	//AI helped with this one
	private static <E extends Enum<E>> E parseEnum(final Class<E> type, final String s) {
	    for (E e : type.getEnumConstants()) {
	        if (e.name().equalsIgnoreCase(s)) return e;
	    }
	    throw new IllegalArgumentException("Unbekannter Wert '" + s + "' für " + type.getSimpleName());
	}
	
	public void save(final String path) throws IOException {
		//Thats AI
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(RunningdinnermetamodelPackage.eNS_URI,RunningdinnermetamodelPackage.eINSTANCE);

		Resource res = rs.createResource(URI.createFileURI(path));
		res.getContents().add(model);
		res.save(Collections.emptyMap());
        //Thats AI

	}
	
	public static void main(String[] args) throws IOException {
	    CsvToModel converter = new CsvToModel();
	    converter.convertCsvToModel("C:/Studium/BachelorArbeit/Programme/Eclipse_Workspaces/runtime-gipstests/runningdinnermetamodel/resources/testTeams.csv","C:/Studium/BachelorArbeit/Programme/Eclipse_Workspaces/runtime-gipstests/runningdinnermetamodel/resources/matrix.csv");
	    converter.save("C:/Studium/BachelorArbeit/Programme/Eclipse_Workspaces/runtime-gipstests/runningdinnermetamodel/resources/model-in.xmi");
	}
}






















