package rewin.service.ubsi.gateway;

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;
import rewin.ubsi.cli.Request;
import rewin.ubsi.consumer.Context;
import rewin.ubsi.container.Bootstrap;

/** 
* ServiceGate Tester. 
*/
public class ServiceGateTest { 

    @Before
    public void before() throws Exception {
        Bootstrap.start();
    }

    @After
    public void after() throws Exception {
        Bootstrap.stop();
    }

    /**
    *
    * Method: activeGate(ServiceContext ctx, String group, int port, long start, Map<String,Map<String,Long>> req, String path)
    *
    */
    @Test
    public void testActiveGate() throws Exception {
        //TODO: Test goes here...
    }

    /**
    *
    * Method: removeGate(ServiceContext ctx, int id)
    *
    */
    @Test
    public void testRemoveGate() throws Exception {
        //TODO: Test goes here...
    }

    /**
    *
    * Method: listGates(ServiceContext ctx, String group, List sortby, int skip, int limit)
    *
    */
    @Test
    public void testListGates() throws Exception {
        Context ubsi = Context.request("rewin.ubsi.gateway", "listGates", null, null, 0, 0);
        Object res = ubsi.direct("localhost", Bootstrap.DEFAULT_PORT);
        Request.printJson(res);
    }

    /**
    *
    * Method: listGroupInGate(ServiceContext ctx)
    *
    */
    @Test
    public void testListGroupInGate() throws Exception {
        //TODO: Test goes here...
    }

}
