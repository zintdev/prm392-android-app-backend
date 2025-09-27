-- Tạo bảng User
CREATE TABLE users (
    userid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    passwordhash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phonenumber VARCHAR(15),
    address VARCHAR(255),
    role VARCHAR(50) NOT NULL
);

-- Tạo bảng Category
CREATE TABLE categories (
    categoryid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    categoryname VARCHAR(100) NOT NULL
);

-- Tạo bảng Product
CREATE TABLE products (
    productid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    productname VARCHAR(100) NOT NULL,
    briefdescription VARCHAR(255),
    fulldescription TEXT,
    technicalspecifications TEXT,
    price DECIMAL(18,2) NOT NULL,
    imageurl VARCHAR(255),
    categoryid INT,
    FOREIGN KEY (categoryid) REFERENCES categories(categoryid)
);

-- Tạo bảng Cart
CREATE TABLE carts (
    cartid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    userid INT,
    totalprice DECIMAL(18,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (userid) REFERENCES users(userid)
);

-- Tạo bảng CartItem
CREATE TABLE cartitems (
    cartitemid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cartid INT,
    productid INT,
    quantity INT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (cartid) REFERENCES carts(cartid),
    FOREIGN KEY (productid) REFERENCES products(productid)
);

-- Tạo bảng Order
CREATE TABLE orders (
    orderid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cartid INT,
    userid INT,
    paymentmethod VARCHAR(50) NOT NULL,
    billingaddress VARCHAR(255) NOT NULL,
    orderstatus VARCHAR(50) NOT NULL,
    orderdate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cartid) REFERENCES carts(cartid),
    FOREIGN KEY (userid) REFERENCES users(userid)
);

-- Tạo bảng Payment
CREATE TABLE payments (
    paymentid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orderid INT,
    amount DECIMAL(18,2) NOT NULL,
    paymentdate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paymentstatus VARCHAR(50) NOT NULL,
    FOREIGN KEY (orderid) REFERENCES orders(orderid)
);

-- Tạo bảng Notification
CREATE TABLE notifications (
    notificationid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    userid INT,
    message VARCHAR(255),
    isread BOOLEAN NOT NULL DEFAULT FALSE,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userid) REFERENCES users(userid)
);

-- Tạo bảng ChatMessage
CREATE TABLE chatmessages (
    chatmessageid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    userid INT,
    message TEXT,
    sentat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userid) REFERENCES users(userid)
);

-- Tạo bảng StoreLocation
CREATE TABLE storelocations (
    locationid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    address VARCHAR(255) NOT NULL
);
