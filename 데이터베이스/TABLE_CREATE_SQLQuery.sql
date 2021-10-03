CREATE TABLE Library(
		Lid_id int PRIMARY KEY,
		Lid_name varchar(30),
		Lib_loc varchar(100)
)
CREATE TABLE Book(
	b_name varchar(30),
	b_id int PRIMARY KEY,
	b_category varchar(100),
	lid_lds int REFERENCES Library(Lid_id)
)
CREATE TABLE Custom(
    id varchar(15) PRIMARY KEY,
	name varchar(15),
	passwd varchar(15),
	b_ids int REFERENCES Book(b_id),
	rental bit
)
CREATE TABLE Dlvy(
	dlvy_id varchar(30) PRIMARY KEY,
	dlvy_name varchar(30),
	dlvy_passwd varchar(15),
	cuid varchar(15) REFERENCES Custom(id)
)
CREATE TABLE Admin(
	a_id int PRIMARY KEY,
	l_id int REFERENCES Library(Lid_id),
	b_id int REFERENCES Book(b_id),
	c_id varchar(15) REFERENCES Custom(id),
	d_id varchar(30) REFERENCES Dlvy(dlvy_id)
)