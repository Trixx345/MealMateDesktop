-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: meal_booking_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `booking`
--

DROP TABLE IF EXISTS `booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `meal_id` int NOT NULL,
  `meal_date` date NOT NULL,
  `quantity` int DEFAULT '1',
  `status` varchar(10) DEFAULT 'BOOKED',
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `student_id` (`student_id`,`meal_id`,`meal_date`),
  KEY `meal_id` (`meal_id`),
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`),
  CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`meal_id`) REFERENCES `meal` (`meal_id`),
  CONSTRAINT `booking_chk_1` CHECK ((`quantity` > 0)),
  CONSTRAINT `booking_chk_2` CHECK ((`status` in (_utf8mb4'BOOKED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking`
--

LOCK TABLES `booking` WRITE;
/*!40000 ALTER TABLE `booking` DISABLE KEYS */;
INSERT INTO `booking` VALUES (1,1,3,'2026-10-27',1,'BOOKED');
/*!40000 ALTER TABLE `booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mbs_bookings`
--

DROP TABLE IF EXISTS `mbs_bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mbs_bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `meal_id` int NOT NULL,
  `quantity` int NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `booking_status` enum('BOOKED','CANCELLED','COLLECTED') NOT NULL DEFAULT 'BOOKED',
  `booked_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `booking_date` date DEFAULT NULL,
  `time_slot` varchar(30) DEFAULT NULL,
  `special_instructions` varchar(500) DEFAULT NULL,
  `cancelled_at` timestamp NULL DEFAULT NULL,
  `collected_at` timestamp NULL DEFAULT NULL,
  `pickup_code` varchar(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `uq_mbs_bookings_pickup_code` (`pickup_code`),
  KEY `student_id` (`student_id`),
  KEY `meal_id` (`meal_id`),
  KEY `idx_mbs_booking_date` (`booked_at`),
  KEY `idx_mbs_booking_status` (`booking_status`),
  KEY `idx_mbs_bookings_date` (`booking_date`),
  CONSTRAINT `mbs_bookings_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `mbs_students` (`student_id`),
  CONSTRAINT `mbs_bookings_ibfk_2` FOREIGN KEY (`meal_id`) REFERENCES `mbs_meals` (`meal_id`),
  CONSTRAINT `mbs_bookings_chk_1` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mbs_bookings`
--

LOCK TABLES `mbs_bookings` WRITE;
/*!40000 ALTER TABLE `mbs_bookings` DISABLE KEYS */;
INSERT INTO `mbs_bookings` VALUES (1,1,1,2,170.00,'BOOKED','2026-10-27 10:33:24','2026-10-27',NULL,NULL,NULL,NULL,'MM-000001'),(2,2,3,2,130.00,'BOOKED','2026-10-27 10:33:24','2026-10-27',NULL,NULL,NULL,NULL,'MM-000002'),(3,3,4,1,55.00,'CANCELLED','2026-10-27 10:33:24','2026-10-27',NULL,NULL,'2026-10-27 12:50:55',NULL,'MM-000003'),(4,4,2,1,110.00,'BOOKED','2026-10-27 10:33:24','2026-10-27',NULL,NULL,NULL,NULL,'MM-000004'),(5,5,6,3,135.00,'BOOKED','2026-10-27 10:33:24','2026-10-27',NULL,NULL,NULL,NULL,'MM-000005'),(6,15,21,1,20.00,'COLLECTED','2026-10-27 10:46:11','2026-10-27',NULL,NULL,NULL,'2026-10-27 12:50:49','MM-000006'),(7,1,3,1,65.00,'COLLECTED','2026-10-27 12:22:09','2026-10-27','Snacks','make it cheesy uk what i mean',NULL,'2026-10-27 12:50:38','MM-000007'),(8,1,24,1,110.00,'CANCELLED','2026-10-27 12:52:58','2026-10-27','Breakfast',NULL,'2026-10-27 13:16:12',NULL,'MM-000008'),(9,1,17,1,65.00,'COLLECTED','2026-10-27 13:16:27','2026-10-27','Lunch',NULL,NULL,'2026-10-27 13:23:54','MM-000009'),(10,1,20,1,75.00,'COLLECTED','2026-10-27 13:22:50','2026-10-27','Breakfast',NULL,NULL,'2026-10-27 13:23:49','MM-918770'),(11,25,15,1,75.00,'COLLECTED','2026-10-27 13:51:39','2026-10-27','Lunch',NULL,NULL,'2026-10-27 13:52:26','MM-840872'),(12,25,20,1,75.00,'COLLECTED','2026-10-28 08:50:31','2026-10-28','Breakfast',NULL,NULL,'2026-10-28 08:52:04','MM-554906');
/*!40000 ALTER TABLE `mbs_bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mbs_meals`
--

DROP TABLE IF EXISTS `mbs_meals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mbs_meals` (
  `meal_id` int NOT NULL AUTO_INCREMENT,
  `meal_name` varchar(100) NOT NULL,
  `category` varchar(40) NOT NULL,
  `food_type` enum('VEG','NON_VEG','EGG','UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
  `description` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `available_qty` int NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`meal_id`),
  CONSTRAINT `mbs_meals_chk_1` CHECK ((`price` >= 0)),
  CONSTRAINT `mbs_meals_chk_2` CHECK ((`available_qty` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mbs_meals`
--

LOCK TABLES `mbs_meals` WRITE;
/*!40000 ALTER TABLE `mbs_meals` DISABLE KEYS */;
INSERT INTO `mbs_meals` VALUES (1,'Veg Thali','Lunch','VEG','Rice, dal, vegetables and chapati',85.00,38,1,'2026-10-27 10:28:59'),(2,'Paneer Rice Bowl','Lunch','VEG','Paneer curry with rice',110.00,24,1,'2026-10-27 10:28:59'),(3,'Masala Dosa','Breakfast','VEG','Dosa with potato filling',65.00,27,1,'2026-10-27 10:28:59'),(4,'Veg Sandwich','Snacks','VEG','Grilled vegetable sandwich',55.00,35,1,'2026-10-27 10:28:59'),(5,'Chicken Biryani','Lunch','NON_VEG','Basmati rice and chicken',145.00,20,1,'2026-10-27 10:28:59'),(6,'Idli Sambar','Breakfast','VEG','Idli, sambar and chutney',45.00,37,0,'2026-10-27 10:28:59'),(7,'Fruit Bowl','Healthy','VEG','Seasonal fresh fruit',70.00,18,1,'2026-10-27 10:28:59'),(8,'Lemon Rice','Lunch','VEG','South Indian lemon rice',60.00,28,1,'2026-10-27 10:28:59'),(9,'Poha','Breakfast','VEG','Flattened rice with peanuts and herbs',35.00,35,1,'2026-10-27 10:31:51'),(10,'Upma','Breakfast','VEG','Semolina cooked with vegetables',40.00,30,1,'2026-10-27 10:31:51'),(11,'Aloo Paratha','Breakfast','VEG','Potato-stuffed paratha with curd',60.00,25,1,'2026-10-27 10:31:51'),(12,'Veg Pulao','Lunch','VEG','Rice with mixed vegetables',80.00,30,1,'2026-10-27 10:31:51'),(13,'Rajma Chawal','Lunch','VEG','Kidney bean curry with rice',90.00,25,1,'2026-10-27 10:31:51'),(14,'Chole Bhature','Lunch','VEG','Chickpea curry with bhature',95.00,20,1,'2026-10-27 10:31:51'),(15,'Dal Khichdi','Lunch','VEG','Lentils and rice',75.00,27,1,'2026-10-27 10:31:51'),(16,'Veg Momos','Snacks','VEG','Steamed vegetable dumplings',70.00,30,1,'2026-10-27 10:31:51'),(17,'French Fries','Snacks','VEG','Crispy potato fries',65.00,24,1,'2026-10-27 10:31:51'),(18,'Cheese Burger','Snacks','VEG','Vegetable patty with cheese',100.00,20,1,'2026-10-27 10:31:51'),(19,'Fresh Lime Soda','Beverages','VEG','Fresh lime drink',40.00,40,1,'2026-10-27 10:31:51'),(20,'Cold Coffee','Beverages','VEG','Chilled coffee with milk',75.00,23,1,'2026-10-27 10:31:51'),(21,'Masala Chai','Beverages','VEG','Indian spiced tea',20.00,49,1,'2026-10-27 10:31:51'),(22,'Sprouts Salad','Healthy','VEG','Mixed sprouts with vegetables',55.00,20,0,'2026-10-27 10:31:51'),(23,'Oats Bowl','Healthy','VEG','Oats with milk and fruit',65.00,18,1,'2026-10-27 10:31:51'),(24,'aandbhat','Healthy','EGG','chef special',110.00,20,0,'2026-10-27 12:51:35'),(25,'chicken salad','Lunch','NON_VEG',NULL,200.00,20,1,'2026-10-30 04:16:23');
/*!40000 ALTER TABLE `mbs_meals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mbs_payments`
--

DROP TABLE IF EXISTS `mbs_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mbs_payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int DEFAULT NULL,
  `student_id` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_type` enum('BOOKING','REFUND','WALLET_TOPUP') NOT NULL,
  `payment_status` enum('SUCCESS','FAILED') DEFAULT 'SUCCESS',
  `payment_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `booking_id` (`booking_id`),
  KEY `idx_mbs_payments_student_time` (`student_id`,`payment_time`),
  CONSTRAINT `mbs_payments_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `mbs_bookings` (`booking_id`),
  CONSTRAINT `mbs_payments_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `mbs_students` (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mbs_payments`
--

LOCK TABLES `mbs_payments` WRITE;
/*!40000 ALTER TABLE `mbs_payments` DISABLE KEYS */;
INSERT INTO `mbs_payments` VALUES (1,1,1,170.00,'BOOKING','SUCCESS','2026-10-27 10:33:24','Demo booking 1'),(2,2,2,130.00,'BOOKING','SUCCESS','2026-10-27 10:33:24','Demo booking 2'),(3,3,3,55.00,'BOOKING','SUCCESS','2026-10-27 10:33:24','Demo booking 3'),(4,4,4,110.00,'BOOKING','SUCCESS','2026-10-27 10:33:24','Demo booking 4'),(5,5,5,135.00,'BOOKING','SUCCESS','2026-10-27 10:33:24','Demo booking 5'),(6,6,15,20.00,'BOOKING','SUCCESS','2026-10-27 10:46:11','Meal booking payment'),(7,7,1,65.00,'BOOKING','SUCCESS','2026-10-27 12:22:09','Meal booking payment'),(8,NULL,8,500.00,'WALLET_TOPUP','SUCCESS','2026-10-27 12:23:38','Administrative demo wallet credit'),(9,3,3,55.00,'REFUND','SUCCESS','2026-10-27 12:50:55','Booking cancellation refund'),(10,8,1,110.00,'BOOKING','SUCCESS','2026-10-27 12:52:58','Meal booking payment'),(11,8,1,110.00,'REFUND','SUCCESS','2026-10-27 13:16:12','Booking cancellation refund'),(12,9,1,65.00,'BOOKING','SUCCESS','2026-10-27 13:16:27','Meal booking payment'),(13,10,1,75.00,'BOOKING','SUCCESS','2026-10-27 13:22:50','Meal booking payment'),(14,11,25,75.00,'BOOKING','SUCCESS','2026-10-27 13:51:39','Meal booking payment'),(15,NULL,25,100.00,'WALLET_TOPUP','SUCCESS','2026-10-28 08:48:19','Administrative demo wallet credit'),(16,12,25,75.00,'BOOKING','SUCCESS','2026-10-28 08:50:31','Meal booking payment'),(17,NULL,27,100.00,'WALLET_TOPUP','SUCCESS','2026-10-30 04:15:20','Administrative demo wallet credit');
/*!40000 ALTER TABLE `mbs_payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mbs_students`
--

DROP TABLE IF EXISTS `mbs_students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mbs_students` (
  `student_id` int NOT NULL AUTO_INCREMENT,
  `roll_no` varchar(30) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(120) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `wallet_balance` decimal(10,2) NOT NULL DEFAULT '0.00',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `roll_no` (`roll_no`),
  UNIQUE KEY `email` (`email`),
  CONSTRAINT `mbs_students_chk_1` CHECK ((`wallet_balance` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mbs_students`
--

LOCK TABLES `mbs_students` WRITE;
/*!40000 ALTER TABLE `mbs_students` DISABLE KEYS */;
INSERT INTO `mbs_students` VALUES (1,'MB2026001','Aarav Sharma','aarav@example.com','9876501001',125.00,'2026-10-27 10:28:59'),(2,'MB2026002','Ananya Patel','ananya@example.com','9876501002',220.00,'2026-10-27 10:28:59'),(3,'MB2026003','Rohan Singh','rohan@example.com','9876501003',250.00,'2026-10-27 10:28:59'),(4,'MB2026004','Priya Nair','priya@example.com','9876501004',290.00,'2026-10-27 10:28:59'),(5,'MB2026005','Kabir Mehta','kabir@example.com','9876501005',165.00,'2026-10-27 10:28:59'),(6,'MB2026006','Isha Verma','isha@example.com','9876501006',200.00,'2026-10-27 10:28:59'),(7,'MB2026007','Dev Kulkarni','dev@example.com','9876501007',450.00,'2026-10-27 10:28:59'),(8,'MB2026008','Sara Khan','sara@example.com','9876501008',775.00,'2026-10-27 10:28:59'),(9,'MB2026009','Neha Desai','neha.desai@example.com','9876501009',320.00,'2026-10-27 10:31:05'),(10,'MB2026010','Arjun Rao','arjun.rao@example.com','9876501010',275.00,'2026-10-27 10:31:05'),(11,'MB2026011','Meera Joshi','meera.joshi@example.com','9876501011',500.00,'2026-10-27 10:31:05'),(12,'MB2026012','Aditya Shah','aditya.shah@example.com','9876501012',180.00,'2026-10-27 10:31:05'),(13,'MB2026013','Kavya Iyer','kavya.iyer@example.com','9876501013',420.00,'2026-10-27 10:31:05'),(14,'MB2026014','Rahul Gupta','rahul.gupta@example.com','9876501014',350.00,'2026-10-27 10:31:05'),(15,'MB2026015','Aditi Chavan','aditi.chavan@example.com','9876501015',205.00,'2026-10-27 10:31:05'),(16,'MB2026016','Vivek Mishra','vivek.mishra@example.com','9876501016',600.00,'2026-10-27 10:31:05'),(17,'MB2026017','Sneha Reddy','sneha.reddy@example.com','9876501017',290.00,'2026-10-27 10:31:05'),(18,'MB2026018','Karan Malhotra','karan.m@example.com','9876501018',375.00,'2026-10-27 10:31:05'),(19,'MB2026019','Pooja Kulkarni','pooja.k@example.com','9876501019',250.00,'2026-10-27 10:31:05'),(20,'MB2026020','Manav Sethi','manav.sethi@example.com','9876501020',450.00,'2026-10-27 10:31:05'),(21,'MB2026021','Riya Kapoor','riya.kapoor@example.com','9876501021',300.00,'2026-10-27 10:31:05'),(22,'MB2026022','Siddharth Jain','sid.jain@example.com','9876501022',525.00,'2026-10-27 10:31:05'),(23,'MB2026023','Tanvi More','tanvi.more@example.com','9876501023',210.00,'2026-10-27 10:31:05'),(24,'MB2026024','Omkar Patil','omkar.patil@example.com','9876501024',400.00,'2026-10-27 10:31:05'),(25,'MB2026025','modeetsapehia','modeetsapehiar@example.com','9876501025',280.00,'2026-10-27 10:31:05'),(26,'MB2026026','Yash Bansal','yash.bansal@example.com','9876501026',260.00,'2026-10-27 10:31:05'),(27,'MB2026027','Diya Menon','diya.menon@example.com','9876501027',580.00,'2026-10-27 10:31:05'),(28,'MB2026028','Harsh Vora','harsh.vora@example.com','9876501028',190.00,'2026-10-27 10:31:05');
/*!40000 ALTER TABLE `mbs_students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mbs_users`
--

DROP TABLE IF EXISTS `mbs_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mbs_users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(80) NOT NULL,
  `password_hash` char(64) NOT NULL,
  `user_role` enum('ADMIN','STUDENT') NOT NULL,
  `student_id` int DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `student_id` (`student_id`),
  CONSTRAINT `fk_mbs_users_student` FOREIGN KEY (`student_id`) REFERENCES `mbs_students` (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mbs_users`
--

LOCK TABLES `mbs_users` WRITE;
/*!40000 ALTER TABLE `mbs_users` DISABLE KEYS */;
INSERT INTO `mbs_users` VALUES (1,'admin','P2$uelGmHthL_sJMw4YEWH2vg$0947b08a1c6fcdf761a6fe96bbcc7150','ADMIN',NULL,1,'2026-10-27 10:53:30'),(2,'MB2026001','P2$qb7aZIjRVSpeCORC25Jszw$7ef1578dbfcf0afea82ddfec5600957f','STUDENT',1,1,'2026-10-27 10:53:30'),(3,'MB2026002','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',2,1,'2026-10-27 10:53:30'),(4,'MB2026003','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',3,1,'2026-10-27 10:53:30'),(5,'MB2026004','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',4,1,'2026-10-27 10:53:30'),(6,'MB2026005','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',5,1,'2026-10-27 10:53:30'),(7,'MB2026006','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',6,1,'2026-10-27 10:53:30'),(8,'MB2026007','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',7,1,'2026-10-27 10:53:30'),(9,'MB2026008','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',8,1,'2026-10-27 10:53:30'),(10,'MB2026009','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',9,1,'2026-10-27 10:53:30'),(11,'MB2026010','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',10,1,'2026-10-27 10:53:30'),(12,'MB2026011','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',11,1,'2026-10-27 10:53:30'),(13,'MB2026012','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',12,1,'2026-10-27 10:53:30'),(14,'MB2026013','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',13,1,'2026-10-27 10:53:30'),(15,'MB2026014','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',14,1,'2026-10-27 10:53:30'),(16,'MB2026015','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',15,1,'2026-10-27 10:53:30'),(17,'MB2026016','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',16,1,'2026-10-27 10:53:30'),(18,'MB2026017','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',17,1,'2026-10-27 10:53:30'),(19,'MB2026018','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',18,1,'2026-10-27 10:53:30'),(20,'MB2026019','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',19,1,'2026-10-27 10:53:30'),(21,'MB2026020','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',20,1,'2026-10-27 10:53:30'),(22,'MB2026021','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',21,1,'2026-10-27 10:53:30'),(23,'MB2026022','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',22,1,'2026-10-27 10:53:30'),(24,'MB2026023','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',23,1,'2026-10-27 10:53:30'),(25,'MB2026024','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',24,1,'2026-10-27 10:53:30'),(26,'riyom','P2$fGtBQDzeOsunRHyU3hqcwA$40a13b529a8ca4f0928ec96c45728e42','STUDENT',25,1,'2026-10-27 10:53:30'),(27,'MB2026026','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',26,1,'2026-10-27 10:53:30'),(28,'yusuf','P2$tkw72BQKPRryPqozf_FSSA$48262fd85209d12fa7f83aa08ef65a9d','STUDENT',27,1,'2026-10-27 10:53:30'),(29,'MB2026028','703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b','STUDENT',28,1,'2026-10-27 10:53:30');
/*!40000 ALTER TABLE `mbs_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `meal`
--

DROP TABLE IF EXISTS `meal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `meal` (
  `meal_id` int NOT NULL AUTO_INCREMENT,
  `meal_name` varchar(50) NOT NULL,
  `meal_type` varchar(15) DEFAULT NULL,
  `price` decimal(8,2) DEFAULT NULL,
  `daily_limit` int NOT NULL,
  PRIMARY KEY (`meal_id`),
  CONSTRAINT `meal_chk_1` CHECK ((`meal_type` in (_utf8mb4'Breakfast',_utf8mb4'Lunch',_utf8mb4'Snacks',_utf8mb4'Dinner'))),
  CONSTRAINT `meal_chk_2` CHECK ((`price` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `meal`
--

LOCK TABLES `meal` WRITE;
/*!40000 ALTER TABLE `meal` DISABLE KEYS */;
INSERT INTO `meal` VALUES (1,'Poha','Breakfast',40.00,50),(2,'Veg Thali','Lunch',80.00,3),(3,'Paneer Rice Bowl','Lunch',120.00,20);
/*!40000 ALTER TABLE `meal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `mode` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `booking_id` (`booking_id`),
  CONSTRAINT `payment_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
  CONSTRAINT `payment_chk_1` CHECK ((`mode` in (_utf8mb4'Wallet',_utf8mb4'UPI',_utf8mb4'Cash')))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,1,120.00,'Wallet');
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `student_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `prn` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `wallet_bal` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `prn` (`prn`),
  UNIQUE KEY `email` (`email`),
  CONSTRAINT `student_chk_1` CHECK ((`wallet_bal` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,'Aarav Sharma','PRN001','aarav@mwu.edu',380.00),(2,'Riya Patil','PRN002','riya@mwu.edu',300.00),(3,'Karan Mehta','PRN003','karan@mwu.edu',200.00);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-30 11:55:00
