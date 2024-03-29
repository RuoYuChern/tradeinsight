create table sys_status(
name varchar(32) NOT NULL,
status varchar(2) NOT NULL
);


create table data_delta_date(
name varchar(32) primary key,
data_date date NOT NULL
);

create table cn_stock_info(
id int generated by default as identity primary key,
status varchar(2) NOT NULL,
symbol varchar(16) NOT NULL,
name varchar(32) NOT NULL,
industry varchar(64) NOT NULL,
market varchar(64) NOT NULL,
exchange varchar(8) NOT NULL,
list_date date NOT NULL,
create_date date NOT NULL
);

create table cn_stock_daily(
id bigint generated by default as identity primary key,
symbol varchar(16) NOT NULL,
name varchar(32) NOT NULL,
close_price decimal(10,2) NOT NULL,
open_price decimal(10,2) NOT NULL,
low_price decimal(10,2) NOT NULL,
high_price decimal(10,2) NOT NULL,
pre_close decimal(10,2) NOT NULL,
change decimal(10,2) NOT NULL,
pct_chg decimal(10,2) NOT NULL,
vol decimal(10,2) NOT NULL,
amount decimal(10,2) NOT NULL,
profit decimal(10,2) NOT NULL,
trade_date date NOT NULL
);

create table cn_market_daily(
id bigint generated by default as identity primary key,
listing int NOT NULL,
delisting int NOT NULL,
up int NOT NULL,
upLimit int NOT NULL,
down int NOT NULL,
downLimit int NOT NULL,
vol decimal(20,6) NOT NULL,
amount decimal(20,6) NOT NULL,
profit decimal(20,6) NOT NULL,
shibor decimal(10,6) NOT NULL,
libor decimal(10,6) NOT NULL,
hibor decimal(10,6) NOT NULL,
trade_date date NOT NULL
);

create table cn_stock_daily_stat(
id bigint generated by default as identity primary key,
symbol varchar(16) NOT NULL,
price decimal(10,2) NOT NULL,
sma_price decimal(10,2) NOT NULL,
ma_price decimal(10,2) NOT NULL,
ema_price decimal(10,2) NOT NULL,
wma_price decimal(10,2) NOT NULL,
trade_date date NOT NULL
);


create table cn_stock_ipo(
id int generated by default as identity primary key,
symbol varchar(16) NOT NULL,
name varchar(32) NOT NULL,
ipo_date date NOT NULL,
issue_date date NOT NULL,
amount decimal(10,2) NOT NULL,
market_amount decimal(10,2) NOT NULL,
price decimal(10,2) NOT NULL,
pe decimal(10,2) NOT NULL,
limit_amount decimal(10,2) NOT NULL,
funds decimal(10,2) NOT NULL,
ballot decimal(10,2) NOT NULL
);

create table quaint_trading(
id int generated by default as identity primary key,
status int NOT NULL,
symbol varchar(16) NOT NULL,
buy_date date NOT NULL,
sell_date date NOT NULL,
alert_price decimal(20,6) NOT NULL,
buy_price decimal(20,6) NOT NULL,
sell_price decimal(20,6) NOT NULL,
strategy varchar(16) NOT NULL
);

create table quaint_find(
id int generated by default as identity primary key,
symbol varchar(16) NOT NULL,
status int NOT NULL,
trade_date date NOT NULL,
close_price decimal(20,6) NOT NULL,
strategy varchar(16) NOT NULL
);


CREATE UNIQUE INDEX sys_name ON sys_status(name);
create UNIQUE index csds_sdt_idx ON cn_stock_daily_stat(symbol, trade_date);
create index csds_date_time_idx ON cn_stock_daily_stat(trade_date);
create UNIQUE index csi_sdt_idx ON cn_stock_ipo(symbol);
create index csi_date_time_idx ON cn_stock_ipo(issue_date);
create UNIQUE index cmd_dt_idx ON cn_market_daily(trade_date);
create UNIQUE index cst_sdt_idx ON cn_stock_daily(symbol, trade_date);
create index cst_date_time_idx ON cn_stock_daily(trade_date);
create index csd_ctime_idx ON cn_stock_info(create_date);
CREATE UNIQUE INDEX csd_symbol_mkt ON cn_stock_info(symbol);

CREATE UNIQUE INDEX qt_sbd_uidx ON quaint_trading(symbol,buy_date);
CREATE INDEX qt_bd_idx ON quaint_trading(buy_date);

CREATE UNIQUE INDEX qf_sbd_uidx ON quaint_find(symbol,trade_date);
CREATE INDEX qf_td_idx ON quaint_find(trade_date);


ALTER TABLE public.cn_market_daily ADD uplimit int NOT NULL DEFAULT 0;

