# GPSPool

## Developers:
+ Dat Huynh
+ Avijit Ghosh
+ Rashika Ramola

## Introduction
The GPS, or Global Positioning System, is an old but reliable system that is used by almost every electronic device to obtain geolocation information. However, although accurate, GPS is an old technology and is extremely power consuming especially for battery operated devices. The capability of mobile phones to join networks and share information can be leveraged to pool the GPS resources among them.  For a group of devices in the network, instead of using GPS of all the devices to query current location, we propose to distribute the work of between the devices. Only one device (leader) queries its GPS at one point, while the others obtain this location from leader via Bluetooth. The leadership rotates between all devices and the duration of leadership is proportional each device's battery. We implement this system through an Android application and we observe that the system saves battery collectively.


## Proposal Framework
We propose a framework that only requires a single device from a set of devices to query GPS signals and multi-casts to others via low-energy Bluetooth channels. We define the notation of fairness as the number of messages each device needs to send which is proportional to the device battery level. Thus low battery devices can prolong their operational time while still contribute GPS signals for the group of devices. When a device finishes sending GPS signals, it will transfer the control to the next device. 

## Contribution
+ We propose the notation of fairness as the workload of each device is proportional to its battery level to prolong operation time of all devices
+ We propose a protocol for synchronous communication for co-ordination between devices
    + We introduce 4 types of messages (Leader, Ack, GPS, Transfer) that convey different information among devices
    + We develop a scheduling algorithm that collects all devices' battery levels and self-computes the number of GPS messages thus the algorithm constantly adapts to the battery level of each device
    + We use Bluetooth protocol for inter-device communication to conserve battery

## Library:
To send Bluetooth signals between devices, we use the Ayanda( https://github.com/sabzo/ayanda ) which is an open-source Android library that offers API for Bluetooth communication among devices.

