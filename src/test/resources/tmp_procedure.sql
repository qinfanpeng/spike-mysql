use ies;

drop procedure if exists tmp_procedure;
create procedure tmp_procedure()
  BEGIN
    DECLARE done BOOLEAN DEFAULT 0;
    DECLARE all_connections TEXT DEFAULT '';
    DECLARE connection_id VARCHAR(300) DEFAULT '';
    DECLARE connected_equipments VARCHAR(600) DEFAULT '';
    DECLARE input_inststance_id VARCHAR(255) DEFAULT 'eq_2';

    DECLARE equipment_cursor CURSOR FOR select relative_id from equipments where instance_id=input_inststance_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN equipment_cursor;

    WHILE done != 1 DO
      FETCH equipment_cursor INTO connection_id;

      IF done != 1 THEN
        SET all_connections = CONCAT(all_connections, connection_id);
        SET all_connections = CONCAT(all_connections, '\n');

        select group_concat(instance_id separator '  \n') into connected_equipments from equipments where relative_id = connection_id;

        SET all_connections = CONCAT(all_connections, connected_equipments);
        SET all_connections = CONCAT(all_connections, '\n----------\n');
      END IF;
    end while;

    select all_connections;

    CLOSE equipment_cursor;

  END;


call tmp_procedure();

DROP PROCEDURE IF EXISTS tmp_procedure;