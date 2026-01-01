package com.watermelon.player.network

import org.fourthline.cling.UpnpService
import org.fourthline.cling.UpnpServiceImpl
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.support.model.DLNA
import org.fourthline.cling.support.renderingcontrol.LastChangeParser
import org.fourthline.cling.support.renderingcontrol.RenderingControl
import org.fourthline.cling.support.model.Channel
import org.fourthline.cling.support.model.ResolutionInformation
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.transport.spi.NetworkAddressFactory

class DLNADiscoverer {
    private val upnpService: UpnpService = UpnpServiceImpl()
    private val controlPoint: ControlPoint = upnpService.controlPoint

    fun startDiscovery() {
        upnpService.registry.addListener { device ->
            if (device is RemoteDevice) {
                // Found DLNA device
                onDeviceFound(device)
            }
        }
        controlPoint.search()
    }

    private fun onDeviceFound(device: RemoteDevice) {
        // Check if it's a media renderer
        val rendererService = device.findService(RenderingControl::class.java)
        if (rendererService != null) {
            // Add to list
        }
    }

    fun stopDiscovery() {
        upnpService.shutdown()
    }
}
