/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  My Local Weather Station
 *
 *  Author: Alan Brown
 *
 *  Date: 2016-09-04
 */
 
 import java.net.URI

metadata {
	definition (name: "Local_Weather_Station", namespace: "dalanbrown", author: "Alan Brown") {
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Sensor"

		attribute "city", "string"
        attribute "state", "string"
        attribute "country", "string"
		attribute "timeZoneOffset", "string"
		attribute "windDirection", "string"
        attribute "windSpeed", "string"
        attribute "wind", "string"
		attribute "feelsLike", "string"
        attribute "pressure", "string"
		attribute "percentPrecip", "string"
        attribute "asOf", "string"

		command "refresh"
	}

	preferences {
        input ("url", "text", title: "URL", description: "The URL to query")
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 1, height: 1, canChangeIcon: true) {
			state "default", label:'${currentValue}°', icon: 'st.Weather.weather2',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}

		valueTile("humidity", "device.humidity", width: 1, height: 1, decoration: "flat") {
			state "default", label:'${currentValue}% humidity', icon: 'st.Weather.weather12'
		}

		valueTile("feelsLike", "device.feelsLike", width: 1, height: 1, decoration: "flat") {
			state "default", label:'feels like ${currentValue}°', icon: 'st.Weather.weather2'
		}

/*
		multiAttributeTile(name:"wind", type:"generic", width:6, height:4) {
        	tileAttribute("device.windSpeed", key: "PRIMARY_CONTROL") {
            	attributeState "default", label:'Wind Speed: ${currentValue} MPH', defaultState: true, backgroundColors:[
					[value: 0, color: "#ff0000"],
					[value: 20, color: "#ffff00"],
					[value: 40, color: "#00ff00"],
					[value: 60, color: "#00ffff"],
					[value: 80, color: "#0000ff"],
					[value: 100, color: "#ff00ff"]
				]
			}
            tileAttribute("device.windDirection", key: "SECONDARY_CONTROL") {
            	attributeState "default", label:'Wind Direction: ${currentValue}°'
            } 
        }
*/

		valueTile("wind", "device.wind", decoration: "flat") {
			state "default", label:'${currentValue}', icon: 'st.Outdoor.outdoor20'
		}

		valueTile("pressure", "device.pressure", width: 1, height: 1, decoration: "flat") {
			state "default", label:'Pressure ${currentValue} in', icon: 'st.Weather.weather15'
		}

		valueTile("city", "device.city", width: 1, height: 1, decoration: "flat") {
			state "default", label:'${currentValue}', icon:"st.Office.office5"
		}
        
        valueTile("asOf", "device.asOf", width: 3, height: 1, decoration: "flat") {
			state "multiLineText", label:'${currentValue}', icon: "st.Weather.weather11"
		}

		standardTile("refresh", "device.weather", width: 1, height: 1, decoration: "flat") {
			state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
		}

		main(["temperature", "weatherIcon"])
		details(["asOf", "temperature", "humidity", "feelsLike", "wind",, "pressure", "city", "refresh"])}
}

private toCompass(direction) {
        def ret = "Unknown"
        def directions = [
                [ value: (0.0f..11.25f), name: 'N' ],
                [ value: (11.25f..33.75f), name: 'NNE' ],
                [ value: (33.75f..56.25f), name: 'NE' ],
                [ value: (56.25f..78.75f), name: 'ENE' ],
                [ value: (78.75f..101.25f), name: 'E' ], 
                [ value: (101.25f..123.75f), name: 'ESE' ],
                [ value: (123.75f..146.25f), name: 'SE' ],
                [ value: (146.25f..168.75f), name: 'SSE' ],
                [ value: (168.75f..191.25f), name: 'S' ],
                [ value: (191.25f..213.75f), name: 'SSW' ],
                [ value: (213.75f..236.25f), name: 'SW' ],
                [ value: (236.25f..258.75f), name: 'WSW' ],
                [ value: (258.75f..281.25f), name: 'W' ],
                [ value: (281.25f..303.75f), name: 'WNW' ],
                [ value: (303.75f..326.25f), name: 'NW' ],
                [ value: (326.25f..348.75f), name: 'NNW' ],
                [ value: (348.75f..360.0f), name: 'N' ],
        ]
        
        // BigDecimal dir = ((direction as double) % 360.0f)
        directions.each() {
                if (it.value.containsWithinBounds(direction as double)) {
                        ret = it.name
                        return
                }
        }
        return ret
}

// parse events into attributes
def parse(String description) {
	// log.debug "Parsing '${description}'"
    
    def msg = parseLanMessage(description)
    // log.debug "Msg = ${msg}"
    // def xml = new XmlSlurper().parseText(msg.body)
    def xml = new XmlParser().parseText(msg.body)
    // log.debug "XML = ${xml}"
    def channel = xml.results.channel
    def units = channel.'yweather:units'[0].attributes()
    def wind = channel.'yweather:wind'[0].attributes()
    def condition = channel.item.'yweather:condition'[0].attributes()
    def atmosphere = channel.'yweather:atmosphere'[0].attributes()
    def location = channel.'yweather:location'[0].attributes()
    def item = channel.item
    def deviceDescription = item.title.text()
    // def attributes = atmosphere[0].attributes()
    // def humidity = atmosphere['humidity']
    
    // log.debug "atmosphere = ${atmosphere}"
    // log.debug "humidity = ${humidity}"
    // log.debug "attr = ${attributes}"
    
    // log.debug "Humidity: ${atmosphere['humidity']}"
    // log.debug "Published: ${item.pubDate.text()}"

	send(name: "temperature", value: condition['temp'].toFloat(), unit: units['temperature'].toString())
	send(name: "feelsLike", value: wind['chill'].toFloat(), unit: units['temperature'].toString())
    send(name: "pressure", value: atmosphere['pressure'].toFloat(), unit: units['pressure'].toString())
	send(name: "humidity", value: atmosphere['humidity'].toFloat(), unit: "%")
	send(name: "windSpeed", value: wind['speed'].toFloat(), unit: units['speed'].toString()) // as String because of bug in determining state change of 0 numbers
	send(name: "windDirection", value: wind['direction'].toFloat(), unit: "°")
    send(name: "wind", value: "Wind ${toCompass(wind['direction'].toFloat())} @ ${wind['speed']}${units['speed']}", unit: "")

	// if (obs.local_tz_offset != device.currentValue("timeZoneOffset")) {
		// send(name: "timeZoneOffset", value: obs.local_tz_offset, isStateChange: true)
	//}

	def cityValue = "${location.city.toString()}, ${location.region.toString()}"
	if (cityValue != device.currentValue("city")) {
		send(name: "city", value: cityValue, isStateChange: true)
	}

	def utf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
	utf.setTimeZone(TimeZone.getTimeZone("GMT"))

	def asOf = deviceDescription + '\n' + utf.parse(item.pubDate.text())
	send(name: "asOf", value: asOf, descriptionText: deviceDescription);
}

def installed() {
	runPeriodically(3600, poll)
}

def uninstalled() {
	unschedule()
}

// handle commands
def poll() {
	log.debug "Local Station: Executing 'poll'"

	return get()
}

def refresh() {
	return poll()
}

def configure() {
	return poll()
}

private pad(String s, size = 25) {
	def n = (size - s.size()) / 2
	if (n > 0) {
		def sb = ""
		n.times {sb += " "}
		sb += s
		n.times {sb += " "}
		return sb
	}
	else {
		return s
	}
}

// gets the address of the hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	log.debug("Convert hex to ip: $hex") 
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
    log.debug device.deviceNetworkId
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private get() {
    log.debug("URL: ${url}")
    
    // Hack up the string into useful parts
    // http://ip:port/path
    // where http implies port 80 and https implies port 443 and everything else is an error
    URI uri = new URI(url)
    
    def scheme = uri.getScheme()
    def host = uri.getHost()
    def path = uri.getPath()
    def port = uri.getPort()
    def query = uri.getQuery()
    def fragment = uri.getFragment()
    
    if (scheme != 'http') {
    	//error
    }
    
    if (null == port) {
    	port = 80
    }
    
    def myPath = ""
    
    if (null != path) {
    	myPath += "${path}"
    }
 
 	if (null != query) {
    	myPath += "?${query}"
    }
    
    if (null != fragment) {
    	myPath += "#${fragment}"
    }
    
    def hosthex = convertIPtoHex(host).toUpperCase() //thanks to @foxxyben for catching this
    def porthex = convertPortToHex(port).toUpperCase()
    device.deviceNetworkId = "${hosthex}:${porthex}" 
    
    try {
        def hubAction = new physicalgraph.device.HubAction(
            method:		"GET",
            path: 		myPath, // "/weather/wview.xml" 
            headers:	[
                HOST:		"${hosthex}:${porthex}", // "192.168.8.228:80",
                Accept: 	"*/*" // "application/xml"
            ])
      
    	// sendHubCommand(hubAction)
        return hubAction
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
}

private localDate(timeZone) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	df.setTimeZone(TimeZone.getTimeZone(timeZone))
	df.format(new Date())
}

private send(map) {
	log.debug "Local Station: event: $map"
	sendEvent(map)
}

