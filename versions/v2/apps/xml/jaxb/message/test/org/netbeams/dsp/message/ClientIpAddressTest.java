package org.netbeams.dsp.message;

import junit.framework.TestCase;

import org.netbeams.dsp.util.NetworkUtil;

public class ClientIpAddressTest extends TestCase {

    public void testIpAddress() {
        assertEquals("130.212.150.216", NetworkUtil.getCurrentEnvironmentNetworkIp());
    }
}
