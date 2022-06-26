/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


CREATE DATABASE IF NOT EXISTS `tuning` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT ENCRYPTION='N';
USE `tuning`;


CREATE TABLE IF NOT EXISTS `급여` (
  `사원번호` int NOT NULL,
  `연봉` int NOT NULL,
  `시작일자` date NOT NULL,
  `종료일자` date NOT NULL,
  `사용여부` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '',
  PRIMARY KEY (`사원번호`,`시작일자`),
  KEY `I_사용여부` (`사용여부`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



CREATE TABLE IF NOT EXISTS `부서` (
  `부서번호` char(4) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `부서명` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `비고` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`부서번호`) USING BTREE,
  UNIQUE KEY `UI_부서명` (`부서명`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `부서관리자` (
  `사원번호` int NOT NULL,
  `부서번호` char(4) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `시작일자` date NOT NULL,
  `종료일자` date NOT NULL,
  PRIMARY KEY (`사원번호`,`부서번호`) USING BTREE,
  KEY `I_부서번호` (`부서번호`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `부서사원_매핑` (
  `사원번호` int NOT NULL,
  `부서번호` char(4) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `시작일자` date NOT NULL,
  `종료일자` date NOT NULL,
  PRIMARY KEY (`사원번호`,`부서번호`) USING BTREE,
  KEY `I_부서번호` (`부서번호`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `사원` (
  `사원번호` int NOT NULL,
  `생년월일` date NOT NULL,
  `이름` varchar(14) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `성` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `성별` enum('M','F') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `입사일자` date NOT NULL,
  PRIMARY KEY (`사원번호`) USING BTREE,
  KEY `I_입사일자` (`입사일자`) USING BTREE,
  KEY `I_성별_성` (`성별`,`성`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `사원출입기록` (
  `순번` int NOT NULL AUTO_INCREMENT,
  `사원번호` int NOT NULL,
  `입출입시간` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `입출입구분` char(1) NOT NULL,
  `출입문` char(1) DEFAULT NULL,
  `지역` char(1) DEFAULT NULL,
  PRIMARY KEY (`순번`,`사원번호`) USING BTREE,
  KEY `I_지역` (`지역`),
  KEY `I_시간` (`입출입시간`),
  KEY `I_출입문` (`출입문`)
) ENGINE=InnoDB AUTO_INCREMENT=1508154 DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `직급` (
  `사원번호` int NOT NULL,
  `직급명` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `시작일자` date NOT NULL,
  `종료일자` date DEFAULT NULL,
  PRIMARY KEY (`사원번호`,`직급명`,`시작일자`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;


SELECT 'LOADING emp.sql' as 'INFO';
source emp.sql ;
SELECT 'LOADING dept.sql' as 'INFO';
source dept.sql ;
SELECT 'LOADING emp_hist1.sql' as 'INFO';
source emp_hist1.sql ;
SELECT 'LOADING emp_hist2.sql' as 'INFO';
source emp_hist2.sql ;
SELECT 'LOADING grade.sql' as 'INFO';
source grade.sql ;
SELECT 'LOADING sal1.sql' as 'INFO';
source sal1.sql ;
SELECT 'LOADING sal2.sql' as 'INFO';
source sal2.sql ;
SELECT 'LOADING sal3.sql' as 'INFO';
source sal3.sql ;
SELECT 'LOADING sal4.sql' as 'INFO';
source sal4.sql ;
SELECT 'LOADING sal5.sql' as 'INFO';
source sal5.sql ;
SELECT 'LOADING sal6.sql' as 'INFO';
source sal6.sql ;
