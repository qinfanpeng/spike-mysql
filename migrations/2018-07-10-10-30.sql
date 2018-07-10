create database spike_mysql;
use spike_mysql;

create table star2002_innodb
(
  antiNucleus int null,
  eventFile int null,
  eventNumber int not null,
  eventTime double null,
  histFile int null,
  multiplicity int null,
  NaboveLb int null,
  NbelowLb int null,
  NLb int null,
  primaryTracks int null,
  prodTime double null,
  Pt double null,
  runNumber int null,
  vertexX double null,
  vertexY double null,
  vertexZ double null,
  id int auto_increment
    primary key
)
;

create index index_eventNumber
  on star2002_innodb (eventNumber)
;

alter table star2002_innodb partition by range (id) (
  partition p0 values less than (1500000),
  partition p1 values less than (2000000),
  partition p2 values less than (3500000),
  partition p3 values less than (5000000),
  partition p4 values less than (6500000),
  partition p5 values less than (8000000),
  partition p6 values less than (9500000),
  partition p7 values less than (11000000),
  partition p8 values less than (12500000),
  partition p9 values less than MAXVALUE
);

create table star2002_myisam
(
  antiNucleus int null,
  eventFile int null,
  eventNumber int not null,
  eventTime double null,
  histFile int null,
  multiplicity int null,
  NaboveLb int null,
  NbelowLb int null,
  NLb int null,
  primaryTracks int null,
  prodTime double null,
  Pt double null,
  runNumber int null,
  vertexX double null,
  vertexY double null,
  vertexZ double null,
  id int auto_increment
    primary key
)
  engine=MyISAM
;

create index index_eventNumber
  on star2002_myisam (eventNumber)
;

alter table star2002_myisam partition by range (id) (
  partition p0 values less than (1500000),
  partition p1 values less than (2000000),
  partition p2 values less than (3500000),
  partition p3 values less than (5000000),
  partition p4 values less than (6500000),
  partition p5 values less than (8000000),
  partition p6 values less than (9500000),
  partition p7 values less than (11000000),
  partition p8 values less than (12500000),
  partition p9 values less than MAXVALUE
);
