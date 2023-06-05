DELETE from STORAGES;

INSERT INTO STORAGES(id, path, bucket, type, created_date)
    VALUES(199, '/files', 'test-bucket-1', 0,'2023-05-30T19:00');

INSERT INTO STORAGES(id, path, bucket, type, created_date)
    VALUES(200, '/files', 'test-bucket-2', 1,'2023-05-31T19:00');