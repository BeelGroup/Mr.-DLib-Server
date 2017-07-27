package main.java.org.mrdlib.partnerContentManager.mediatum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Static class offering the service to save progress of harvesting mediaTUM's ws_export interface.
 */
class ProgressSavingService {

    /**
     * Saves progress of exploring nodes into given file.
     * 
     * @param nodeIds Hash map containing progress to save.
     * @param progressFilePath Path of file to write progress to.
     */
    static void saveProgress(HashMap<String, ExplorationState> nodeIds, String progressFilePath) {
        try{
            PrintWriter writer = new PrintWriter(progressFilePath, "UTF-8");

            for (String nodeId : nodeIds.keySet()) {
                String line = nodeId;

                // indicate that the node has already been explored
                if (nodeIds.get(nodeId) == ExplorationState.EXPLORED) {
                    line = "[explored] " + nodeId;
                }

                writer.println(line);
            }

            writer.close();
        } catch (IOException e) {
            ConsoleOutputService.printOutError("Error while saving progress to file " + progressFilePath +
                    ".", e);
        }
    }

    /**
     * Reads in the progress from the given file and returns it as a hash map.
     * 
     * @param progressFilePath File to read in progress from.
     * @return Hash map containing the read in progress.
     */
    static HashMap<String, ExplorationState> readInProgress(String progressFilePath) {
        HashMap<String, ExplorationState> nodeIds = new HashMap<>();

        try {
            Scanner scanner = new Scanner(new File(progressFilePath));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // distinguish state
                if (line.startsWith("[explored]")) {
                    // explored
                    nodeIds.putIfAbsent(line.replace("[explored] ", ""), ExplorationState.EXPLORED);
                } else {
                    // unexplored
                    nodeIds.putIfAbsent(line, ExplorationState.NOT_EXPLORED);
                }
            }
            
            scanner.close();
        } catch (FileNotFoundException e) {
            ConsoleOutputService.printOutError("Error while reading in progress file " + progressFilePath +
                    ".", e);
        }
        
        return nodeIds;
    }

}
