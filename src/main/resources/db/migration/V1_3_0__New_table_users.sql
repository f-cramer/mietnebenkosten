CREATE TABLE "users" (
    "username" character varying(255) NOT NULL,
    "password" character varying(255) NOT NULL,
    CONSTRAINT "pk_users" PRIMARY KEY ("username")
);
