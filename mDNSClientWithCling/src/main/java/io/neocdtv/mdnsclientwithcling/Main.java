/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.mdnsclientwithcling;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

/**
 * Runs a simple UPnP discovery procedure.
 */
public class Main {

  public static void main(String[] args) throws Exception {

    // UPnP discovery is asynchronous, we need a callback
    RegistryListener listener;
    listener = new RegistryListener() {
      
      @Override
      public void remoteDeviceDiscoveryStarted(Registry registry,
              RemoteDevice device) {
        System.out.println(
                "Discovery started: " + device.getDisplayString()
        );
      }

      @Override
      public void remoteDeviceDiscoveryFailed(Registry registry,
              RemoteDevice device,
              Exception ex) {
        System.out.println(
                "Discovery failed: " + device.getDisplayString() + " => " + ex
        );
      }

      @Override
      public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

        System.out.println(
                "Remote device available: " + device.getDisplayString()
        );
      }

      @Override
      public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        System.out.println(
                "Remote device updated: " + device.getDisplayString()
        );
      }

      @Override
      public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        System.out.println(
                "Remote device removed: " + device.getDisplayString()
        );
      }

      @Override
      public void localDeviceAdded(Registry registry, LocalDevice device) {
        System.out.println(
                "Local device added: " + device.getDisplayString()
        );
      }

      @Override
      public void localDeviceRemoved(Registry registry, LocalDevice device) {
        System.out.println(
                "Local device removed: " + device.getDisplayString()
        );
      }

      @Override
      public void beforeShutdown(Registry registry) {
        System.out.println(
                "Before shutdown, the registry has devices: "
                        + registry.getDevices().size()
        );
      }

      @Override
      public void afterShutdown() {
        System.out.println("Shutdown of registry complete!");

      }
    };

    // This will create necessary network resources for UPnP right away
    System.out.println("Starting Cling...");
    UpnpService upnpService = new UpnpServiceImpl(listener);
    

    // Send a search message to all devices and services, they should respond soon
    upnpService.getControlPoint().search(new STAllHeader());

    System.out.println("Press q and Enter, to quit");
    int b;
    while ((b = System.in.read()) != -1 && (char) b != 'q') {
      /* Stub */
    }

    // Release all resources and advertise BYEBYE to other UPnP devices
    System.out.println("Stopping Cling...");
    upnpService.shutdown();
  }
}
