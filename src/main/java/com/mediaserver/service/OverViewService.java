// OverViewService.java
package com.mediaserver.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediaserver.model.Content;
import com.mediaserver.model.Device;
import com.mediaserver.model.Playlist;
import com.mediaserver.repository.ContentRepository;
import com.mediaserver.repository.DeviceRepository;
import com.mediaserver.repository.PlaylistRepository;

@Service
public class OverViewService {

    // @Autowired
    // private GroupDeviceRepository groupDeviceRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private ContentRepository contentRepository;

    //  public List<GroupDevice> getGroupByUserId(String userId) {
    //     return groupDeviceRepository.findByUserId(userId);
    // }

    public  int getTheDeviceCount(String userId){
        List<Device> deviceList = deviceRepository.findByUserId(userId);
        return deviceList.size();
    }

     public  int getTheOnlineDeviceCount(String status){
        List<Device> deviceList = deviceRepository.findByStatus(status);
        return deviceList.size();
    }

    public int getThePlayListCount(String userId){
        List<Playlist> playlistList = playlistRepository.findByUserId(userId);
        return playlistList.size();
    }

    public int getTheMediaCount(String userId){
        List<Content> contentList = contentRepository.findByUserId(userId);
        return (int) contentList.stream().filter(c -> !c.isSystemGenerated()).count();
    }
    
    public int getTheDevicestatus(String userId, String status){
        List<Device> deviceList = deviceRepository.findByUserIdAndStatus(userId, status);
        return deviceList.size();
    }

    public int getTotalDeviceCount(){
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.size();
    }

    public int getTotalOnlineDeviceCount(){
        List<Device> deviceList = deviceRepository.findByStatus("online");
        return deviceList.size();
    }

    public int getTotalPlayListCount(){
        List<Playlist> playlistList = playlistRepository.findAll();
        return playlistList.size();
    }

    public int getTotalMediaCount(){
        List<Content> contentList = contentRepository.findAll();
        return (int) contentList.stream().filter(c -> !c.isSystemGenerated()).count();
    }

}
