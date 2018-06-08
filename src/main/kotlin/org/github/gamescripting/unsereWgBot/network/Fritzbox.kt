package org.github.gamescripting.unsereWgBot.network

import de.mapoll.javaAVMTR064.FritzConnection
import org.springframework.stereotype.Component

@Component
class Fritzbox(
        fritzTR064Config: FritzTR064Config
) {
    final val fritzConnection: FritzConnection

    init {
        fritzConnection = FritzConnection(
                fritzTR064Config.address,
                fritzTR064Config.user,
                fritzTR064Config.password
        )
        fritzConnection.init()
    }

    fun getMacAdressesInNetwork(): List<String> {
        return readNetworkDevices()
                .filter { it.active == true }
                .map { it.macAddress }
                .filterNotNull()
    }

    fun readNetworkDevices(): List<HostInfo> {
        val hostsService = fritzConnection.getService("Hosts:1") ?: throw Exception("Hosts:1 not supported")

        var index = 0
        return generateSequence {
            try {
                val data = hostsService.getAction("GetGenericHostEntry").execute(mapOf("NewIndex" to index++)).data
                HostInfo(
                        data["NewMACAddress"],
                        data["NewHostName"],
                        data["NewAddressSource"],
                        data["NewActive"]?.let { it == "1" },
                        data["NewIPAddress"],
                        data["NewLeaseTimeRemaining"]?.toIntOrNull(),
                        data["NewInterfaceType"]
                )
            } catch (e: Exception) {
                null
            }
        }.toList()
    }
}

data class HostInfo(
        val macAddress: String?,
        val hostName: String?,
        val addressSource: String?,
        val active: Boolean?,
        val ipAddress: String?,
        val leaseTimeRemaining: Int?,
        val interfaceType: String?
)