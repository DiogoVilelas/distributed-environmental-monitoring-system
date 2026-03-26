package pt.uevora.sd.project.server.dto;

import jakarta.validation.constraints.*;

public class DeviceRequest {

    @NotBlank
    private String id;

    @NotBlank
    private String protocol; 

    @NotBlank
    private String building;

    @Min(0)
    private int floor;

    @NotBlank
    private String department;

    @NotBlank
    private String room;

    @NotBlank
    private String status;

    public DeviceRequest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
