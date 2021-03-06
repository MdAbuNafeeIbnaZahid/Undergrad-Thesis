package simulation.experiment;

import deep_copy.UnoptimizedDeepCopy;
import graph.Graph;
import graph.Node;
import graph.graph_type.GraphFactory;
import helper_util.MyUtil;
import helper_util.Validator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import server_placing.RandomServerPlacing;
import server_placing.STNServerPlacing;
import server_placing.ServerPlacing;
import server_placing.ServerPlacingFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nafee on 12/19/17.
 */
public abstract class FindingNumberOfServersNeeded extends Experiment {

    int simulationCnt = 5;
    int serverDropAssumption = 2;
    XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
    double densityPercent;
    Graph graph;

    public FindingNumberOfServersNeeded(int serverRange)
    {
        super(serverRange);
        if ( serverRange < 0 )
        {
            throw new IllegalArgumentException(" serverRange can't be negative ");
        }
    }


    public FindingNumberOfServersNeeded(int nodeCnt, int serverRange)
    {
        super(nodeCnt, serverRange);

        Validator.validateNodeCnt(nodeCnt);

        if ( serverRange < 0 )
        {
            throw new IllegalArgumentException(" serverRange can't be negative ");
        }
    }



    private XYSeries getXySeries(ServerPlacing serverPlacing)
    {
        assert serverPlacing != null;
        System.out.println(serverPlacing);


        XYSeries xySeries = new XYSeries( serverPlacing.toString() );


        for (double varyingParameter = 5; varyingParameter <= 100; varyingParameter += 1)
        {
            List<Integer> serverReqList = new ArrayList<Integer>();

            for (int differentGraphSimulationCnt = 0; differentGraphSimulationCnt < simulationCnt; differentGraphSimulationCnt++ )
            {
                createAndAssignGraph(varyingParameter);

                for ( int sameGraphSimulationCnt = 0; sameGraphSimulationCnt < simulationCnt; sameGraphSimulationCnt++ )
                {
                    Graph copyGraph = (Graph) UnoptimizedDeepCopy.copy(graph);
                    // While operation we may need to change graph. So rather than changing the main graph
                    // passed as parameter to the method getXySeries, we are using a copy of the graph
                    // and changing it

                    int currentServerReq = serverPlacing.getServerCntForGoodConnectivity(copyGraph, serverRange, serverDropAssumption+1);
                    assert currentServerReq >= 0;

                    serverReqList.add( currentServerReq );
                }
            }

            Double avgServerReq = serverReqList.stream().mapToDouble(val -> val).average().getAsDouble();

            System.out.println("serverReq = " + avgServerReq);


            xySeries.add(varyingParameter, avgServerReq);
        }

        return xySeries;
    }

    @Override
    public void doExperiment() {

        ServerPlacing randomServerPlacing = ServerPlacingFactory.getRandomServerPlacing();
       generateAndAddCurveInXYSeriesCollection(randomServerPlacing);

       ServerPlacing paperServerPlacing = ServerPlacingFactory.getPaperServerPlacing();
       generateAndAddCurveInXYSeriesCollection(paperServerPlacing);


       for (double degreeWeight = 0; degreeWeight <= 1; degreeWeight += 0.33 )
       {
           double weakNodeCoverCntWeight = 1 - degreeWeight;
           ServerPlacing stnServerPlacing = ServerPlacingFactory.getSTNServerPlacing(weakNodeCoverCntWeight, degreeWeight);
           generateAndAddCurveInXYSeriesCollection(stnServerPlacing);
       }

        createJFreeChart();
    }

    void generateAndAddCurveInXYSeriesCollection( ServerPlacing serverPlacing )
    {
        XYSeries xySeries = getXySeries(serverPlacing);
        xySeriesCollection.addSeries(xySeries);
    }

    final void createJFreeChart()
    {
        jFreeChart = ChartFactory.createXYLineChart(getName(), getXAxisLabel(),
                getYAxisLabel(), xySeriesCollection, PlotOrientation.VERTICAL, true,
                true, false);
    }

    abstract void createAndAssignGraph(double varyingParameter);

    abstract String getXAxisLabel();

    String getYAxisLabel()
    {
        return "serversNeeded";
    }
}
