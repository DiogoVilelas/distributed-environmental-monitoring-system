package pt.uevora.sd.project.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uevora.sd.project.server.model.Device;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, String> {

    @Query("SELECT d.id FROM Device d WHERE LOWER(d.room) = LOWER(:room)")
    List<String> findIdsByRoomIgnoreCase(@Param("room") String room);

    @Query("SELECT d.id FROM Device d WHERE LOWER(d.department) = LOWER(:dept)")
    List<String> findIdsByDepartmentIgnoreCase(@Param("dept") String dept);

    @Query("SELECT d.id FROM Device d WHERE LOWER(d.building) = LOWER(:building)")
    List<String> findIdsByBuildingIgnoreCase(@Param("building") String building);

    @Query("SELECT d.id FROM Device d WHERE LOWER(d.building) = LOWER(:building) AND d.floor = :floor")
    List<String> findIdsByBuildingAndFloor(@Param("building") String building, @Param("floor") int floor);
}
