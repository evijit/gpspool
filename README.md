# GPSPool

## Developers:
+ Dat Huynh
+ Avijit Ghosh
+ Rashika Ramola

## Introduction
The GPS, or Global Positioning System, is an old but reliable system that is used by almost every electronic device to obtain geolocation information. However, although accurate, GPS is old technology and is extremely power consuming especially for battery operated devices. Mobile phones being capable of forming local networks between devices, opens up a very interesting opportunity for the reduction of the power consumed by the GPS by pooling this task among phones in a local network, thereby minimizing the power consumption of the group as a whole and eliminating redundant energy wastage for GPS lock.

## Proposal Framework
We propose a framework that only requires only a single device from a set of devices to query GPS signals and multi-cast to others via low-energy bluetooth channels. We define the notation of fairness as the number of messages each device need to send which is the protional the device battery level. Thus low battery devices can prolong their operational time while still contribute GPS signals for the group of devices. When a device finishes sending GPS signal, it will transfer the control to the next device. 

## Contribution
+ We propose the notation of fairness as the work of each device is proportional to its battery level to prolong operation time of all devices
+ We propose a protocol for synchronous communication for co-ordination between devices
	+ We introduces 4 types of messages (Leader, Ack, GPS, Transfer) that convey different information among devices
	+ We develop a scheduling algorithm the collects all devices' battery levels and self-computes the number of GPS messages thus constantly adapting to the battery level of each device
	+ We use bluetooth protocol for inter-device communication to conserve battery

## Library:
To send bluetooth signal between devices, we use the Ayanda( https://github.com/sabzo/ayanda ) which is a open-source Android library that offers API for bluetooth communication among devices.

