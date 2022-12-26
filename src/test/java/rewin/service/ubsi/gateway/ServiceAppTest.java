package rewin.service.ubsi.gateway;

import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import rewin.ubsi.cli.Request;
import rewin.ubsi.consumer.Context;
import rewin.ubsi.container.Bootstrap;

/** 
* ServiceApp Tester. 
*/
public class ServiceAppTest { 

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
    * Method: regApp(ServiceContext ctx, Map app)
    *
    */
    @Test
    public void testRegApp() throws Exception {
        //TODO: Test goes here...
    }

    /**
    *
    * Method: unregApp(ServiceContext ctx, String id)
    *
    */
    @Test
    public void testUnregApp() throws Exception {
        //TODO: Test goes here...
    }

    /**
    *
    * Method: setApp(ServiceContext ctx, String id, Map app)
    *
    */
    @Test
    public void testSetApp() throws Exception {
        //TODO: Test goes here...
    }

    /**
    *
    * Method: getAppTags(ServiceContext ctx)
    *
    */
    @Test
    public void testGetAppTags() throws Exception {
        //TODO: Test goes here...
    }

    /**
    *
    * Method: getApps(ServiceContext ctx, Set<String> ids)
    *
    */
    @Test
    public void testGetApps() throws Exception {
        //TODO: Test goes here...
    }

    /**
    *
    * Method: listApps(ServiceContext ctx, String name, Set<String> tags, int status, List sortby, int skip, int limit)
    *
    */
    @Test
    public void testListApps() throws Exception {
        Context ubsi = Context.request("rewin.ubsi.gateway", "listApps", null, null, 0, null, 0, 0);
        Object res = ubsi.direct("localhost", Bootstrap.DEFAULT_PORT);
        Request.printJson(res);
    }

}
