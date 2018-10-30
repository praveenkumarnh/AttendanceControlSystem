  create table employees
  (
    code      int          not null primary key,
    firstName varchar(25)  not null,
    lastName  varchar(25)  not null,
    email     varchar(125) null,
    avatar    varchar(125) null,
    check(code > 0),
    constraint email
    unique (email)
  );

  create index employee_firstName_idx on employees (firstName);
  create index employee_lastName_idx on employees (lastName);

  create table tracks
  (
    id           varchar(38)                 not null primary key,
    employeeCode int                         not null,
    createdAt    datetime                    not null,
    action       enum ('entrance', 'exit')   not null,
    constraint tracks_ibfk_1
    foreign key (employeeCode) references employees (code)
  );

  create index tracks_createdAt_idx on tracks (createdAt);
  create index tracks_employeeCode_idx on tracks (employeeCode);
  create index tracks_action_idx on tracks `action`);