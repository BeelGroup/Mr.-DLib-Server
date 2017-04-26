package org.mrdlib.partnerContentManager.mediatum.downloader;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Offers the functionality to harvest the custom ws_export interface of mediaTUM.
 */
public class WsExportHarvester {

    /**
     * Returns true if the given URL returns nodes, otherwise false.
     * @param url URL of node.
     * @return True or false.
     */
    private static Boolean areNodesReturned(String url) {
        // retrieve document
        InputStream inputStream = WebsiteRetrievalService.getInputStreamFromUrl(url);

        if (inputStream == null) return false;

        Document document = WebsiteRetrievalService.getDocumentFromInputStream(inputStream);

        // retrieve nodes
        NodeList nodeList = document.getElementsByTagName("node");

        // check if nodes have been returned
        return nodeList.getLength() > 0;
    }

    /**
     * Returns a query string for retrieving parents of a node.
     * @param baseUrl Base URL of the ws_export service.
     * @param nodeId Id of node whose parents to retrieve.
     * @return Query string.
     */
    private static String getParentsRetrievalUrl(String baseUrl, String nodeId) {
        return baseUrl + nodeId + "/parents";
    }

    /**
     * Harvests the parents of the given node in the given ws_export service with the given parameters and saves the
     * progress in the given hash map.
     * @param baseUrl Base URL of the ws_export service.
     * @param nodeIds Hash map for keeping track of the harvesting progress.
     * @param nodeId Id of node whose parents to retrieve.
     */
    private static void harvestParents(String baseUrl, HashMap<String, ExplorationState> nodeIds, String nodeId) {
        String url = getParentsRetrievalUrl(baseUrl, nodeId);

        // retrieve document
        Document document = WebsiteRetrievalService.getDocumentFromInputStream(WebsiteRetrievalService.getInputStreamFromUrl(url));

        // retrieve parent nodes
        NodeList nodeList = document.getElementsByTagName("node");

        // iterate over parent nodes
        for (int i = 0; i < nodeList.getLength(); i++) {
            // extract parent node's id from node
            String parentNodeId = nodeList.item(i).getAttributes().getNamedItem("id").toString()
                    .replace("\"", "").replace("id=", "");

            // add new parent nodes to list
            if (!nodeIds.containsKey(parentNodeId)) {
                nodeIds.put(parentNodeId, ExplorationState.NOT_EXPLORED);
            }
        }
    }

    /**
     * Returns a query string for retrieving children of a node.
     * @param baseUrl Base URL of the ws_export service.
     * @param numNodesToFetchAtOnce Number of nodes to fetch at once.
     * @param nodeId Id of node whose children to retrieve.
     * @param index Index used in query string.
     * @return Query string.
     */
    private static String getChildrenRetrievalUrl(String baseUrl, int numNodesToFetchAtOnce, long nodeId, long index) {
        return baseUrl + nodeId + "/children/?start=" + index + "&limit=" + numNodesToFetchAtOnce;
    }

    /**
     * Harvests the children of the given node in the given ws_export service with the given parameters and saves the
     * progress in the given hash map.
     * @param baseUrl Base URL of the ws_export service.
     * @param numNodesToFetchAtOnce Number of nodes to fetch at once.
     * @param nodeIds Hash map for keeping track of the harvesting progress.
     * @param nodeId Node whose children to harvest.
     */
    private static void harvestChildren(String baseUrl, int numNodesToFetchAtOnce,
                                        HashMap<String, ExplorationState> nodeIds, String nodeId) {
        // index and numNodesToFetchAtOnce are used to retrieve batches of children nodes
        long index = 0;
        String url = getChildrenRetrievalUrl(baseUrl, numNodesToFetchAtOnce, Long.parseLong(nodeId), index);

        // fetch children as long as children can be found
        while (areNodesReturned(url)) {
            // retrieve document
            Document document = WebsiteRetrievalService.getDocumentFromInputStream(
                    WebsiteRetrievalService.getInputStreamFromUrl(url));

            if (document != null) {
                // retrieve children nodes
                NodeList nodeList = document.getElementsByTagName("node");

                // iterate over nodes
                for (int i = 0; i < nodeList.getLength(); i++) {
                    // extract children node's id from node
                    String newNodeId = nodeList.item(i).getAttributes().getNamedItem("id").toString()
                            .replace("\"", "").replace("id=", "");

                    // add new parent nodes to list
                    if (!nodeIds.containsKey(newNodeId)) {
                        nodeIds.put(newNodeId, ExplorationState.NOT_EXPLORED);
                    }
                }
            }

            // for keeping track of progress
            ConsoleOutputService.printOutStatus(nodeId + ": " + (index + numNodesToFetchAtOnce) +
                    " children retrieved");

            // update parameters for next batch of children nodes
            index = index + numNodesToFetchAtOnce;
            url = getChildrenRetrievalUrl(baseUrl, numNodesToFetchAtOnce, Long.parseLong(nodeId), index);
        }
    }

    /**
     * Returns the number of explored nodes in the given hash map.
     * @param nodeIds Hash map for keeping track of the harvesting progress.
     * @return Number of explored nodes.
     */
    private static int countExploredNodes(HashMap<String, ExplorationState> nodeIds) {
        int numExploredNodes = 0;

        for (String nodeId : nodeIds.keySet()) {
            if (nodeIds.get(nodeId) == ExplorationState.EXPLORED) {
                numExploredNodes++;
            }
        }

        return numExploredNodes;
    }

    /**
     * Harvests the given node in the given ws_export service with the given parameters and saves the progress in the
     * given hash map.
     * @param baseUrl Base URL of the ws_export service.
     * @param numNodesToFetchAtOnce Number of nodes to fetch at once.
     * @param nodeIds Hash map for keeping track of the harvesting progress.
     * @param nodeId Node to harvest.
     * @param harvestOnlyChildren Indicates whether only children may be harvested, as opposed to harvesting children
     *                            and parents.
     */
    private static void harvestNode(String baseUrl, int numNodesToFetchAtOnce, HashMap<String, ExplorationState> nodeIds,
                                    String nodeId, boolean harvestOnlyChildren) {
        // mark node as explored
        nodeIds.replace(nodeId, ExplorationState.EXPLORED);

        harvestChildren(baseUrl, numNodesToFetchAtOnce, nodeIds, nodeId);
        if (!harvestOnlyChildren) {
            harvestParents(baseUrl, nodeIds, nodeId);
        }
    }

    /**
     * Returns the key of the first unexplored node found in the given hash map.
     * @param nodeIds Hash map for keeping track of the harvesting progress.
     * @return Key of the first found unexplored node in the given hash map.
     */
    private static String getFirstUnexploredNode(HashMap<String, ExplorationState> nodeIds) {
        String firstUnexploredNode = null;

        for (String currentNodeId : nodeIds.keySet()) {
            if (nodeIds.get(currentNodeId) == ExplorationState.NOT_EXPLORED) {
                firstUnexploredNode = currentNodeId;
                break;
            }
        }

        return firstUnexploredNode;
    }

    /**
     * Harvests the first unexplored node in the given hash map.
     * @param baseUrl Base URL of the ws_export service.
     * @param numNodesToFetchAtOnce Number of nodes to fetch at once.
     * @param nodeIds Hash map for keeping track of the harvesting progress.
     * @param harvestOnlyChildren Indicates whether only children may be harvested, as opposed to harvesting children
     *                            and parents.
     */
    private static void harvestFirstUnexploredNodeId(String baseUrl, int numNodesToFetchAtOnce,
                                                     HashMap<String, ExplorationState> nodeIds,
                                                     boolean harvestOnlyChildren) {
        String firstUnexploredNode = getFirstUnexploredNode(nodeIds);

        // for keeping track of progress
        ConsoleOutputService.printOutStatus("[" + new Date() + "] [number of explored nodes / number of all " +
                "found nodes: " + countExploredNodes(nodeIds) + "/" + nodeIds.size() + " | currently harvested node: " +
                firstUnexploredNode + "]");

        harvestNode(baseUrl, numNodesToFetchAtOnce, nodeIds, firstUnexploredNode, harvestOnlyChildren);
    }

    /**
     * Returns true if nodes in the given hash map are not yet explored, otherwise false.
     * @param nodeIds Hash map to search for unexplored nodes in.
     * @return True or false.
     */
    private static Boolean areNodesUnexplored(HashMap<String, ExplorationState> nodeIds) {
        for (String nodeId : nodeIds.keySet()) {
            if (nodeIds.get(nodeId) == ExplorationState.NOT_EXPLORED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Harvests the ws_export service of mediaTUM with the given parameters.
     * @param baseUrl Base URL of the ws_export service.
     * @param numNodesToFetchAtOnce Number of nodes to fetch at once.
     * @param fileContainingNodesIdsPath File that contains the progress (each line information about a node).
     * @param harvestOnlyChildren Indicates whether only children may be harvested, as opposed to harvesting children
     *                            and parents. This option may be used if the top level nodes are known and thus exploring
     *                            the complete tree structure is unnecessary.
     */
    public static void harvest(String baseUrl, int numNodesToFetchAtOnce, String fileContainingNodesIdsPath, boolean harvestOnlyChildren) {
        // read in progress potentially saved in given file
        HashMap<String, ExplorationState> nodeIds = ProgressSavingService.readInProgress(fileContainingNodesIdsPath);

        // harvest until everything is harvested
        while (areNodesUnexplored(nodeIds)) {
            harvestFirstUnexploredNodeId(baseUrl, numNodesToFetchAtOnce, nodeIds, harvestOnlyChildren);

            ProgressSavingService.saveProgress(nodeIds, fileContainingNodesIdsPath);
        }
    }

}
