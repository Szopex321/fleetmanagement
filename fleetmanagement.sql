--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

-- Started on 2025-06-01 15:36:19

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 222 (class 1259 OID 16928)
-- Name: assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.assignments (
    id integer NOT NULL,
    vehicle_id integer,
    driver_id integer,
    start_date date NOT NULL,
    end_date date,
    destination character varying(255),
    purpose character varying(255),
    start_mileage integer,
    end_mileage integer,
    notes text,
    status character varying(50) DEFAULT 'Zaplanowane'::character varying,
    creation_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT assignments_check CHECK (((end_mileage IS NULL) OR (start_mileage IS NULL) OR (end_mileage >= start_mileage))),
    CONSTRAINT assignments_start_mileage_check CHECK (((start_mileage IS NULL) OR (start_mileage >= 0)))
);


ALTER TABLE public.assignments OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16927)
-- Name: assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.assignments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.assignments_id_seq OWNER TO postgres;

--
-- TOC entry 4833 (class 0 OID 0)
-- Dependencies: 221
-- Name: assignments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.assignments_id_seq OWNED BY public.assignments.id;


--
-- TOC entry 220 (class 1259 OID 16914)
-- Name: drivers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.drivers (
    id integer NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    license_number character varying(50) NOT NULL,
    phone_number character varying(20),
    email character varying(100),
    employment_date date,
    status character varying(50) DEFAULT 'Aktywny'::character varying,
    license_expiry_date date,
    medical_check_expiry_date date,
    address text
);


ALTER TABLE public.drivers OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16913)
-- Name: drivers_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.drivers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.drivers_id_seq OWNER TO postgres;

--
-- TOC entry 4834 (class 0 OID 0)
-- Dependencies: 219
-- Name: drivers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.drivers_id_seq OWNED BY public.drivers.id;


--
-- TOC entry 218 (class 1259 OID 16899)
-- Name: vehicles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicles (
    id integer NOT NULL,
    make character varying(100) NOT NULL,
    model character varying(100) NOT NULL,
    registration_number character varying(20) NOT NULL,
    production_year integer,
    vin character varying(17),
    fuel_type character varying(50),
    status character varying(50) DEFAULT 'Dostępny'::character varying,
    mileage integer,
    last_service_date date,
    insurance_expiry_date date,
    purchase_date date,
    notes text,
    CONSTRAINT vehicles_mileage_check CHECK (((mileage IS NULL) OR (mileage >= 0)))
);


ALTER TABLE public.vehicles OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16898)
-- Name: vehicles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.vehicles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicles_id_seq OWNER TO postgres;

--
-- TOC entry 4835 (class 0 OID 0)
-- Dependencies: 217
-- Name: vehicles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.vehicles_id_seq OWNED BY public.vehicles.id;


--
-- TOC entry 4655 (class 2604 OID 16931)
-- Name: assignments id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assignments ALTER COLUMN id SET DEFAULT nextval('public.assignments_id_seq'::regclass);


--
-- TOC entry 4653 (class 2604 OID 16917)
-- Name: drivers id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.drivers ALTER COLUMN id SET DEFAULT nextval('public.drivers_id_seq'::regclass);


--
-- TOC entry 4651 (class 2604 OID 16902)
-- Name: vehicles id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicles ALTER COLUMN id SET DEFAULT nextval('public.vehicles_id_seq'::regclass);


--
-- TOC entry 4827 (class 0 OID 16928)
-- Dependencies: 222
-- Data for Name: assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.assignments (id, vehicle_id, driver_id, start_date, end_date, destination, purpose, start_mileage, end_mileage, notes, status, creation_date) FROM stdin;
\.


--
-- TOC entry 4825 (class 0 OID 16914)
-- Dependencies: 220
-- Data for Name: drivers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.drivers (id, first_name, last_name, license_number, phone_number, email, employment_date, status, license_expiry_date, medical_check_expiry_date, address) FROM stdin;
\.


--
-- TOC entry 4823 (class 0 OID 16899)
-- Dependencies: 218
-- Data for Name: vehicles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vehicles (id, make, model, registration_number, production_year, vin, fuel_type, status, mileage, last_service_date, insurance_expiry_date, purchase_date, notes) FROM stdin;
\.


--
-- TOC entry 4836 (class 0 OID 0)
-- Dependencies: 221
-- Name: assignments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.assignments_id_seq', 1, false);


--
-- TOC entry 4837 (class 0 OID 0)
-- Dependencies: 219
-- Name: drivers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.drivers_id_seq', 1, false);


--
-- TOC entry 4838 (class 0 OID 0)
-- Dependencies: 217
-- Name: vehicles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.vehicles_id_seq', 1, false);


--
-- TOC entry 4674 (class 2606 OID 16939)
-- Name: assignments assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_pkey PRIMARY KEY (id);


--
-- TOC entry 4668 (class 2606 OID 16926)
-- Name: drivers drivers_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.drivers
    ADD CONSTRAINT drivers_email_key UNIQUE (email);


--
-- TOC entry 4670 (class 2606 OID 16924)
-- Name: drivers drivers_license_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.drivers
    ADD CONSTRAINT drivers_license_number_key UNIQUE (license_number);


--
-- TOC entry 4672 (class 2606 OID 16922)
-- Name: drivers drivers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.drivers
    ADD CONSTRAINT drivers_pkey PRIMARY KEY (id);


--
-- TOC entry 4662 (class 2606 OID 16908)
-- Name: vehicles vehicles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicles
    ADD CONSTRAINT vehicles_pkey PRIMARY KEY (id);


--
-- TOC entry 4664 (class 2606 OID 16910)
-- Name: vehicles vehicles_registration_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicles
    ADD CONSTRAINT vehicles_registration_number_key UNIQUE (registration_number);


--
-- TOC entry 4666 (class 2606 OID 16912)
-- Name: vehicles vehicles_vin_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicles
    ADD CONSTRAINT vehicles_vin_key UNIQUE (vin);


--
-- TOC entry 4675 (class 2606 OID 16945)
-- Name: assignments assignments_driver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_driver_id_fkey FOREIGN KEY (driver_id) REFERENCES public.drivers(id) ON DELETE SET NULL;


--
-- TOC entry 4676 (class 2606 OID 16940)
-- Name: assignments assignments_vehicle_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_vehicle_id_fkey FOREIGN KEY (vehicle_id) REFERENCES public.vehicles(id) ON DELETE SET NULL;


-- Completed on 2025-06-01 15:36:19

--
-- PostgreSQL database dump complete
--

