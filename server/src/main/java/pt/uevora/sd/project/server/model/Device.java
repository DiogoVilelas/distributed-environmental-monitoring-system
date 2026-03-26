package pt.uevora.sd.project.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @Column(length = 64, nullable = false, updatable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Protocol protocol;

    @Column(nullable = false)
    private String building;

    @Column(nullable = false)
    private int floor;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.ACTIVE;

    public Device() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}

