package org.mrdlib.utils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.database.DBConnection;

import javafx.util.Pair;

public class DBModificationScripts {
	
	DBConnection con = null;
	Constants constants = null;
	static int count = 0;
	//This method is tested
	public void updateShuffleStatus(int startingSet, int numberOfSets){
		Pair<List<Long>, List<Boolean>> pair = con.getDocumentSets(startingSet, numberOfSets);
		
		List<Long> recommendationSetIds = pair.getKey();
		if(recommendationSetIds  == null ||recommendationSetIds.size()==0){
			System.out.println("Nothing in this set");
			return;
		}
		List<Boolean> shuffledFlagInDB = con.getShuffledFlagInDB(new Long(startingSet), recommendationSetIds.get(recommendationSetIds.size()-1));
		List<Boolean> shuffled = pair.getValue();

		if(shuffled.equals(shuffledFlagInDB)) System.out.println("All ok");
		/*for(int i = 0; i < shuffled.size(); i ++ ){
			System.out.println(recommendationSetIds.get(i) + ", " + shuffled.get(i) + ", " + shuffledFlagInDB.get(i) 
			+ ", " + (shuffled.get(i)^shuffledFlagInDB.get(i)) );
		}*/
		List<Long> needsFixing = recommendationSetIds.stream()
		.filter(s-> shuffled.get(recommendationSetIds.indexOf(s))^shuffledFlagInDB.get(recommendationSetIds.indexOf(s)))
		.collect(Collectors.toList());
		
		List<Pair<Long, Long>> fixedAlgorithmIds = needsFixing.stream()
				.map(this::updateEntries).collect(Collectors.toList());
		List<Pair<Long, Boolean>> fixedSets = fixedAlgorithmIds.stream()
				.map(this::updateRecommendationSet).collect(Collectors.toList());
		System.out.println(fixedSets);
	}
	
	public Pair<Long, Boolean> updateRecommendationSet(Pair<Long,Long> fixedPair){
		return con.updateRecommendationAlgorithmIdInRecomemndationSet(fixedPair);
	}
	public  Pair<Long, Long> updateEntries(Long id){
		Long newAlgorithmId = con.switchShuffledFlag(id);
		return new Pair<Long, Long>(id, newAlgorithmId);
	}
	public DBModificationScripts(){
		try{
			this.constants = new Constants();
			this.con = new DBConnection("jar");
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Cannot get connection to DB");
		}
	}
	public static void main(String[] args) {
		DBModificationScripts script = new DBModificationScripts();
		script.updateShuffleStatus(10275, 400);
		//script.updateShuffleStatus(10275, 50);
		script.closeConnections();
	}
	
	
	public void closeConnections(){
		if(con!=null)
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
