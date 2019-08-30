package com.bpms;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Main {
    public static final String rest_url = "https://elxkoom6p4.execute-api.eu-central-1.amazonaws.com/prod/engine-rest/process-definition/key/invoice/xml";

    public static List<String> visitedNodes = new ArrayList();
    public static boolean foundPath = false;

    public static String getDataFromAPI(String url) {
        HttpResponse<JsonNode> response = Unirest.get(url).header("accept", "application/json").asJson();
        return (String) response.getBody().getObject().get("bpmn20Xml");
    }


    public static void searchPath(FlowNode current, FlowNode end, String path) {

        if (current.getId().equals(end.getId())) {
            System.out.println(path);
            foundPath = true;
        }
        Collection<SequenceFlow> next = current.getOutgoing();

        for (SequenceFlow i : next) {
            if (visitedNodes.contains(i.getTarget().getId())) continue;

            visitedNodes.add(i.getTarget().getId());
            searchPath(i.getTarget(), end, new String(path + " " + i.getTarget().getId()));

        }
    }

    public static void main(String[] args) {
        String sourceNode = args[0];
        String destinationNode = args[1];
        System.out.println("Computing path from " + sourceNode + " to " + destinationNode);

        String xmlData = getDataFromAPI(rest_url);
        //System.out.println(xmlData);

        InputStream in = new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8));
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(in);


//        FlowNode start = (FlowNode) modelInstance.getModelElementById("approveInvoice");
//        FlowNode end = (FlowNode) modelInstance.getModelElementById("invoiceProcessed");

        FlowNode start = (FlowNode) modelInstance.getModelElementById(sourceNode);
        FlowNode end = (FlowNode) modelInstance.getModelElementById(destinationNode);

        if (start == null || end == null) {
            System.out.println("Invalid start or destination node in input.");
            return;
        }
        visitedNodes.add(start.getId());

        searchPath(start, end, new String(start.getId() + " "));
        if (!foundPath) {
            System.out.println("No path exists between the two nodes");
        }


    }
}
