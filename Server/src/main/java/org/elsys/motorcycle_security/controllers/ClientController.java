package org.elsys.motorcycle_security.controllers;

import org.elsys.motorcycle_security.business.logic.DataTransmiterHandler;
import org.elsys.motorcycle_security.business.logic.UserHandler;
import org.elsys.motorcycle_security.dto.DataTransmiterInfo;
import org.elsys.motorcycle_security.dto.UserInfo;
import org.elsys.motorcycle_security.repository.DeviceConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
public class ClientController {
    @Autowired
    private UserHandler usersHandler;
    @Autowired
    private DataTransmiterHandler dataTransmiterHandler;
    @Autowired
    private DeviceConfigurationRepository deviceConfigurationRepository;

    @RequestMapping(value = "/client/send/parking-status", method = PUT)
    public void updateParkingStatusByDeviceId(@RequestParam(value = "deviceId", defaultValue = "0") long deviceId, @RequestParam(value = "isParked", defaultValue = "0") boolean isParked) {
        deviceConfigurationRepository.updateParkingStatusByDeviceId(deviceId, isParked);
    }

    @RequestMapping(value = "/client/send/timeout", method = PUT)
    public void updateTimeoutByDeviceId(@RequestParam(value = "deviceId", defaultValue = "0") long deviceId, @RequestParam(value = "timeout", defaultValue = "0") long timeout) {
        deviceConfigurationRepository.updateTimeoutByDeviceId(deviceId, timeout);
    }

    @RequestMapping(value = "/client/send/create-new-user", method = POST)
    public void createNewUser(@RequestParam(value = "username", defaultValue = "") String username, @RequestParam(value = "email", defaultValue = "") String email, @RequestParam(value = "password", defaultValue = "") String password, @RequestParam(value = "deviceId", defaultValue = "") String deviceId) {
        usersHandler.createNewUser(username, password, email, deviceId);
    }

    @RequestMapping(value = "/client/receive/{deviceId}/gps-cordinates", method = GET)
    @ResponseBody
    public DataTransmiterInfo getGpsCordinatesBydeviceId(@PathVariable(value = "deviceId") long deviceId) {
        DataTransmiterInfo dataTransmiterInfo = dataTransmiterHandler.getGPSCordinates(deviceId);
        return dataTransmiterInfo;
    }

    @RequestMapping(value="/client/receive/user-account",method=GET)
    @ResponseBody
    public UserInfo getUserAccountByUsername(@RequestHeader("userName") String userName) {
        UserInfo userInfo = usersHandler.getUser(userName);
        return userInfo;
    }
}


