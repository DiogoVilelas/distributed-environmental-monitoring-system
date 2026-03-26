package pt.uevora.sd.project.admincli.dto;

public class DeviceDto {
    public String id;
    public String protocol;    
    public String building;
    public int floor;
    public String department;
    public String room;
    public String status;      

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %d | %s | %s | %s",
                id, protocol, building, floor, department, room, status);
    }
}
