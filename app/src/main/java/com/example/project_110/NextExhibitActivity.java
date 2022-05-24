package com.example.project_110;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NextExhibitActivity extends AppCompatActivity {
    private List<String> detailed_Directions_List;
    private List<String> brief_Directions_List;
    private ArrayAdapter adapter;
    private List<VertexInfoStorable> shortestVertexOrder;
    private Map<String, ZooData.VertexInfo> vInfo;
    private Map<String, ZooData.EdgeInfo> eInfo;
    private Graph<String, IdentifiedWeightedEdge> g;
    private int counter;

    // brief directions list constructor
    public NextExhibitActivity(List<String> brief_directions_list) {
        brief_Directions_List = brief_directions_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_exhibit);

        shortestVertexOrder = getIntent().getParcelableArrayListExtra("shortestVertexOrder");
        vInfo = ZooData.loadVertexInfoJSON(this, "sample_node_info.json");
        eInfo = ZooData.loadEdgeInfoJSON(this, "sample_edge_info.json");
        g = ZooData.loadZooGraphJSON(this, "sample_zoo_graph.json");

        counter = 0;

        String start = shortestVertexOrder.get(counter).id;
        counter +=1;
        String next = shortestVertexOrder.get(counter).id;

        // generate shortest path from start to first exhibit
        GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, next);
        int i = 1;
        detailed_Directions_List = new ArrayList<>();
        for (IdentifiedWeightedEdge e : path.getEdgeList()) {
            String direction = String.format("  %d. Walk %.0f meters along %s from '%s' to '%s'.\n",
                    i,
                    g.getEdgeWeight(e),
                    eInfo.get(e.getId()).street,
                    vInfo.get(g.getEdgeSource(e).toString()).name,
                    vInfo.get(g.getEdgeTarget(e).toString()).name);
            i++;
            detailed_Directions_List.add(direction);
        }
        adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, detailed_Directions_List);
        ListView listView = (ListView) findViewById(R.id.directions_list);
        listView.setAdapter(adapter);


    }

    public void onNextBtnClick(View view) {
        detailed_Directions_List.clear();
        if(counter == shortestVertexOrder.size()-2){
            Button disableNext = (Button) findViewById(R.id.next_button);
            disableNext.setClickable(false);
        }
        String start = shortestVertexOrder.get(counter).id;
        counter+=1;
        String next = shortestVertexOrder.get(counter).id;

        GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, next);
        int i = 1;

        String currVertex = start;

        for (IdentifiedWeightedEdge e : path.getEdgeList()) {
            ZooData.VertexInfo source = vInfo.get(g.getEdgeSource(e).toString());
            ZooData.VertexInfo target = vInfo.get(g.getEdgeTarget(e).toString());
            if (currVertex.equals(target.id)) {
                //swap print statement
                ZooData.VertexInfo temp = target;
                target = source;
                source = temp;
            }

            String direction = String.format("  %d. Walk %.0f meters along %s from '%s' to '%s'.\n",
                    i,
                    g.getEdgeWeight(e),
                    eInfo.get(e.getId()).street,
                    source.name,
                    target.name);
            i++;
            detailed_Directions_List.add(direction);
            currVertex = target.id;
        }
        adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, detailed_Directions_List);
        ListView listView = (ListView) findViewById(R.id.directions_list);
        listView.setAdapter(adapter);
    }

    private void lastExhibit() {
    }

    // getter for brief directions list
    public List<String> getBrief_Directions_List() {
        return brief_Directions_List;
    }

    // setter for brief directions list
    public void setBrief_Directions_List(List<String> brief_Directions_List) {
        this.brief_Directions_List = brief_Directions_List;

        List<String> temp_List_ofID = new ArrayList<>(); // ordering parallels detailed_Directions_List
                                                         // temp list of lists of indicies
        // groups indicies of temp_list_ofIDs, by IDs
        List<List<Integer>> temp_List_ofList = new ArrayList<List<Integer>>();

        // declare list -- algo goes here
        for (String elemStr : detailed_Directions_List) {

            // extract startIndex [this substr] endIndex
            int startIndex = elemStr.indexOf("along")+6; // starting index of string ("along")
                                                         // offset 5 for 'along', offset 1 for space
            int endIndex = elemStr.indexOf("from");      // ending index of string ("from")
            String str_ID = elemStr.substring(startIndex, endIndex); // along [str_ID]

            temp_List_ofID.add(str_ID);
        }
        // for (String str_ID : temp_List_ofID)
        // have: [A,B,A,A,C]
        // want: [[0],[1],[2,3],[4]]
        Integer tL_IDsize = temp_List_ofID.size();
        for (int i=0; i<tL_IDsize; i++) {
            String toCheck = temp_List_ofID.get(i); // check if string is within any list
            List<Integer> tempStrList = new ArrayList<Integer>();
            for (int j=0; j<tL_IDsize; j++) {
                if (toCheck == temp_List_ofID.get(j)) { // found duplicate
                    // add duplicate's index to list of lists
                    tempStrList.add(j);
                }
            }
            temp_List_ofList.add(tempStrList);  // add all indicies of unique string to list of lists
        }

        // now: go through detailed directions list instance, group them by identifiers
    }

} // end of nextBtnClk