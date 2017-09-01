/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.seleniumwithrequesttracing;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.proxy.ProxyServer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 *
 * @author xix
 */
public class Main {
    
    private static final String URL_TEMPLATE = "$..entries[*].request.url";
    
    public static void main(String[] args) throws IOException {
        
        ProxyServer proxy = new ProxyServer(8081);
        proxy.start();
        final Proxy seleniumProxy = proxy.seleniumProxy();
        
        //seleniumProxy.setSslProxy("127.0.0.1:" + proxy.getPort());
        //seleniumProxy.setSocksProxy("127.0.0.1:" + proxy.getPort());
        ProfilesIni profile = new ProfilesIni();
        FirefoxProfile ffProfile = new FirefoxProfile();
        ffProfile.setAcceptUntrustedCertificates(true);
        ffProfile.setAssumeUntrustedCertificateIssuer(true);
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        capabilities.setCapability(FirefoxDriver.PROFILE, profile);

        // start the browser up
        
        //final ChromeDriver driver = new ChromeDriver(capabilities); - how to configure proxy?
        final FirefoxDriver driver = new FirefoxDriver(capabilities);
        proxy.newHar();
        
        driver.get("https://www.google.de");
        
        //enableContentLogging(proxy);
        

        // get the HAR data
        Har har = proxy.getHar();
        
        final StringWriter writerForHar = new StringWriter();
        har.writeTo(writerForHar);
        final String harString = writerForHar.toString();

        System.out.println(harString);
        List<String> urls = JsonPath.parse(harString).read(String.format(URL_TEMPLATE, "google", List.class)); 
        urls.stream().forEach((url) -> {
            System.out.println(url);
        });
        
        driver.close();
        driver.quit();
        System.exit(0);
    }

    private static void enableContentLogging(ProxyServer proxy) {
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
    }
}