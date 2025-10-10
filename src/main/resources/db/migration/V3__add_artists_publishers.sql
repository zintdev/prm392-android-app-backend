-- Tạo bảng Publishers
CREATE TABLE publishers (
    publisher_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    publisher_name VARCHAR(100) NOT NULL,
    founded_year INT NOT NULL
);

-- Tạo bảng Artists
CREATE TABLE artists (
    artist_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    artist_name VARCHAR(100) NOT NULL,
    debut_year INT NOT NULL,
    artist_type VARCHAR(50) NOT NULL
);
