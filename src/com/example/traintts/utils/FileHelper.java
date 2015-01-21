package com.example.traintts.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.example.traintts.DAO.VoiceMapsDataSource;
import com.opencsv.CSVReader;

public class FileHelper {
	@SuppressWarnings("resource")
	public void saveCVSFileToDatasource(InputStreamReader file ,VoiceMapsDataSource datasource){
		String next[] = {};
		try {
		CSVReader reader = new CSVReader(file);//Specify asset file name
		
		// in open();
		for(;;) {
			next = reader.readNext();
			if(next != null) {
				
				// Not end of file
				if (datasource.queryVoiceMapByArgs(
						Integer.parseInt(next[0]), // SEGMENT
						Integer.parseInt(next[1]), // SIGNAL
						Double.parseDouble(next[2]), // DISTANCE
						next[3])
					!= null	){
					
					// Update old record to make segment signal and distance unique
					datasource.updateVoiceMapByArgs(
							Integer.parseInt(next[0]), // SEGMENT
							Integer.parseInt(next[1]), // SIGNAL
							Double.parseDouble(next[2]), // DISTANCE
							next[3]); // VOICE
				} else {
					
					// New record so create a new row by segment signal and distance
					datasource.createVoiceMap(
							Integer.parseInt(next[0]), // SEGMENT
							Integer.parseInt(next[1]), // SIGNAL
							Double.parseDouble(next[2]), // DISTANCE
							next[3]); // VOICE
				}
			} else {
				// End of file
				break;
			}
		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
