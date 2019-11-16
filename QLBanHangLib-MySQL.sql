/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     11/14/2019 1:44:06 AM                        */
/*==============================================================*/


drop table if exists CHI_TIET_HOA_DON;

drop table if exists HANG_HOA;

drop table if exists HOA_DON;

drop table if exists KHACH_HANG_VIP;

drop table if exists PHIEU_NHAP_KHO;

/*==============================================================*/
/* Table: CHI_TIET_HOA_DON                                      */
/*==============================================================*/
create table CHI_TIET_HOA_DON
(
   HH_MA                char(12) not null,
   HD_STT               int not null,
   CTHD_SOLUONG         int not null,
   CTHD_GIABAN          float not null,
   primary key (HH_MA, HD_STT)
);

/*==============================================================*/
/* Table: HANG_HOA                                              */
/*==============================================================*/
create table HANG_HOA
(
   HH_MA                char(12) not null,
   HH_TEN               char(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci not null,
   HH_DONVITINH         char(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci not null,
   HH_DONGIABAN         float not null,
   primary key (HH_MA)
);

INSERT INTO HANG_HOA
VALUES 
	('M049','Mì gói hảo hảo','Gói','3000'),
	('M020','Mì gói 3 miền','Gói','3500'),
	('M050','Mì gói Omachi','Gói','6000'),
	('M026','Mì gói cung đình','Gói','5000'),
	('M010','Mì gói tiến vua','Gói','10000');


/*==============================================================*/
/* Table: HOA_DON                                               */
/*==============================================================*/
create table HOA_DON
(
   HD_STT               int not null AUTO_INCREMENT,
   KH_SOTHE             int,
   HD_NGAYLAPHD         datetime not null,
   HD_TONGGIATRI        float not null,
   primary key (HD_STT)
);

/*==============================================================*/
/* Table: KHACH_HANG_VIP                                        */
/*==============================================================*/
create table KHACH_HANG_VIP
(
   KH_SOTHE             int not null AUTO_INCREMENT,
   KH_HOTEN             char(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci not null,
   KH_GIOITINH          bool,
   KH_SDT               char(15),
   KH_TICHDIEM          int not null,
   primary key (KH_SOTHE)
);

/*==============================================================*/
/* Table: PHIEU_NHAP_KHO                                        */
/*==============================================================*/
create table PHIEU_NHAP_KHO
(
   PNK_STT              int not null AUTO_INCREMENT,
   HH_MA                char(12) not null,
   PNK_NGAYNHAP         datetime not null,
   PNK_DONGIANHAP       float not null,
   PNK_SOLUONGNHAP      int not null,
   primary key (PNK_STT)
);

alter table CHI_TIET_HOA_DON add constraint FK_RELATIONSHIP_2 foreign key (HH_MA)
      references HANG_HOA (HH_MA);

alter table CHI_TIET_HOA_DON add constraint FK_RELATIONSHIP_3 foreign key (HD_STT)
      references HOA_DON (HD_STT);

alter table HOA_DON add constraint FK_RELATIONSHIP_4 foreign key (KH_SOTHE)
      references KHACH_HANG_VIP (KH_SOTHE);

alter table PHIEU_NHAP_KHO add constraint FK_RELATIONSHIP_1 foreign key (HH_MA)
      references HANG_HOA (HH_MA);

