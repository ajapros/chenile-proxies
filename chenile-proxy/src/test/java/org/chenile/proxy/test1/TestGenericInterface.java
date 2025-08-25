package org.chenile.proxy.test1;

import org.chenile.proxy.test1.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = BarSpringConfig.class,webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("unittest")
public class TestGenericInterface {
    @Autowired @Qualifier("barService1Proxy") BarService<Baz1> barService1;
   @Autowired @Qualifier("barService1OnlyRemote") BarService<Baz1> barService1OnlyRemote;
   @Autowired @Qualifier("barService2OnlyRemote") BarService<Baz2> barService2OnlyRemote;

    @Test public void baz1ProxyTest() {
        Baz1 baz = new Baz1();
        BarModel<Baz1> barM ;
        barM = barService1.doubleIt(baz);
        assertEquals(20, barM.baz.getValue());
    }
    @Test public void baz1Test() {
        Baz1 baz = new Baz1();
		BarModel<Baz1> barM ;
		barM = barService1OnlyRemote.doubleIt(baz);
		assertEquals(20, barM.baz.getValue());
    }

    @Test public void baz2Test() {
        Baz2 baz = new Baz2();
        BarModel<Baz2> barM ;
        barM = barService2OnlyRemote.doubleIt(baz);
        assertEquals(40, barM.baz.getValue());
    }

}
