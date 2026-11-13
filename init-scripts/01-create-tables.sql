CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL UNIQUE,
    protein NUMERIC(10,1) NOT NULL DEFAULT 0 CHECK (protein >= 0),
    oil NUMERIC(10,1) NOT NULL DEFAULT 0 CHECK (oil >= 0),
    carb NUMERIC(10,1) NOT NULL DEFAULT 0 CHECK (carb >= 0),
    calorie NUMERIC(10,1) NOT NULL DEFAULT 0 CHECK (calorie >= 0)
);

CREATE TABLE IF NOT EXISTS dishes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS workouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    animation VARCHAR(255),
    workout_type VARCHAR(50) NOT NULL CHECK (workout_type IN (
        'базовое', 'изолирующее', 'калистеника', 'статическое',
        'кардио', 'бег', 'велоспорт', 'плавание', 'гребля', 'ходьба',
        'кроссфит', 'плиометрическое', 'растяжка', 'йога',
        'мобильность', 'разминка', 'заминка'
    ))
);

CREATE TABLE IF NOT EXISTS muscules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS inventories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS part_of_body (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS products_in_dish (
    dish_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quanty SMALLINT NOT NULL CHECK (quanty > 0),
    unit_of_measure VARCHAR(30) NOT NULL CHECK (unit_of_measure IN (
        'г', 'гр', 'грамм', 'грамма', 'граммов',
        'кг', 'килограмм', 'килограмма', 'килограммов',
        'мл', 'миллилитр', 'миллилитра', 'миллилитров',
        'л', 'литр', 'литра', 'литров',
        'шт', 'штука', 'штуки', 'штук',
        'порт', 'порция', 'порции', 'порций',
        'ч. л.', 'ч.л.', 'чайная ложка', 'чайные ложки', 'чайных ложек',
        'ст. л.', 'ст.л.', 'столовая ложка', 'столовые ложки', 'столовых ложек',
        'стакан', 'стакана', 'стаканов',
        'щепотка', 'щепотки', 'щепоток'
    )),
    FOREIGN KEY (dish_id) REFERENCES dishes(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    PRIMARY KEY(dish_id, product_id)
);

CREATE TABLE IF NOT EXISTS dish_tags (
    tag_id UUID,
    dish_id UUID,
    FOREIGN KEY (dish_id) REFERENCES dishes(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE RESTRICT,
    PRIMARY KEY(dish_id, tag_id)
);

CREATE TABLE IF NOT EXISTS workout_tags (
    tag_id UUID,
    workout_id UUID,
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE RESTRICT,
    PRIMARY KEY(workout_id, tag_id)
);

CREATE TABLE IF NOT EXISTS workout_muscules (
    muscul_id UUID,
    workout_id UUID,
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
    FOREIGN KEY (muscul_id) REFERENCES muscules(id) ON DELETE RESTRICT,
    PRIMARY KEY(workout_id, muscul_id)
);

CREATE TABLE IF NOT EXISTS workout_inventories (
    inventory_id UUID,
    workout_id UUID,
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE RESTRICT,
    PRIMARY KEY(workout_id, inventory_id)
);

CREATE TABLE IF NOT EXISTS meals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    meal_type VARCHAR(40) NOT NULL CHECK (meal_type IN (
        'завтрак', 'второй завтрак', 'обед', 'полдник', 'ужин',
        'поздний ужин', 'перекус', 'до тренировки', 'после тренировки'
    )),
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dishes_in_meal (
    dish_id UUID NOT NULL,
    meal_id UUID NOT NULL,
    quanty SMALLINT NOT NULL CHECK (quanty > 0),
    FOREIGN KEY (dish_id) REFERENCES dishes(id) ON DELETE CASCADE,
    FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE RESTRICT,
    PRIMARY KEY(dish_id, meal_id)
);

CREATE TABLE IF NOT EXISTS drunk_water (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL,
    date_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    quanty SMALLINT NOT NULL CHECK (quanty > 0),
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trainings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL,
    date_time_start TIMESTAMP NOT NULL,
    date_time_end TIMESTAMP NOT NULL,
    status VARCHAR(40) NOT NULL,
    feeling SMALLINT CHECK (feeling BETWEEN 1 AND 5),
    description TEXT,
    calories SMALLINT NOT NULL DEFAULT 0 CHECK (calories >= 0),
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS training_workouts (
    training_id UUID NOT NULL,
    workout_id UUID NOT NULL,
    FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE,
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE RESTRICT,
    PRIMARY KEY(training_id, workout_id)
);

CREATE TABLE IF NOT EXISTS exercise_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quanty SMALLINT NOT NULL DEFAULT 0 CHECK (quanty >= 0),
    number_of_repeat SMALLINT NOT NULL DEFAULT 0 CHECK (number_of_repeat >= 0),
    set_number SMALLINT NOT NULL DEFAULT 0 CHECK (set_number >= 0),
    weight SMALLINT NOT NULL DEFAULT 0 CHECK (weight >= 0),
    
    -- Добавляем две колонки вместо одной
    training_id UUID NOT NULL,
    workout_id UUID NOT NULL,
    
    -- Ссылаемся на составной ключ родительской таблицы
    FOREIGN KEY (training_id, workout_id) REFERENCES training_workouts(training_id, workout_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS weights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    weight NUMERIC(5,1) NOT NULL CHECK (weight > 0),
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS progres_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    training_id UUID,
    weight_id UUID,
    
    -- Удаляем старую одну колонку и добавляем две для связи с training_workouts
    tw_training_id UUID,
    tw_workout_id UUID,
    
    url VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (weight_id) REFERENCES weights(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Связываем по составному ключу из двух колонок
    FOREIGN KEY (tw_training_id, tw_workout_id) REFERENCES training_workouts(training_id, workout_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_facts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    height SMALLINT NOT NULL CHECK (height > 50),
    sex CHAR(1) NOT NULL CHECK (sex IN ('м', 'ж')),
    role VARCHAR(30) NOT NULL CHECK (role IN ('тренер', 'пользователь', 'админ')) DEFAULT ('пользователь'),
    date_of_birthday DATE,
    goal VARCHAR(255),
     weight NUMERIC(5,1) CHECK (weight > 0),
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS measurements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    part_of_body_id UUID NOT NULL,
    value SMALLINT NOT NULL CHECK (value > 0),
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (part_of_body_id) REFERENCES part_of_body(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS workout_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS workout_template_slots (
    template_id UUID NOT NULL,
    workout_id UUID NOT NULL,
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE RESTRICT,
    FOREIGN KEY (template_id) REFERENCES workout_templates(id) ON DELETE CASCADE,
    PRIMARY KEY(workout_id, template_id)
);

CREATE TABLE IF NOT EXISTS template_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    set_number SMALLINT NOT NULL CHECK (set_number > 0),
    reps SMALLINT NOT NULL CHECK (reps > 0),
    weight SMALLINT NOT NULL CHECK (weight > 0),
    
    -- Разделяем одну колонку на две для связи с составным ключом
    template_id UUID NOT NULL,
    workout_id UUID NOT NULL,
    
    -- Связываем по паре колонок с workout_template_slots
    FOREIGN KEY (workout_id, template_id) REFERENCES workout_template_slots(workout_id, template_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS workout_template_tags (
    tag_id UUID,
    workout_template_id UUID,
    FOREIGN KEY (workout_template_id) REFERENCES workout_templates(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE RESTRICT,
    PRIMARY KEY(workout_template_id, tag_id)
);

CREATE TABLE IF NOT EXISTS meal_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS meal_template_slots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_template_id UUID NOT NULL,
    type_meal VARCHAR(30) NOT NULL CHECK (type_meal IN (
        'завтрак', 'второй завтрак', 'обед', 'полдник', 'ужин',
        'поздний ужин', 'перекус', 'до тренировки', 'после тренировки'
    )),
    FOREIGN KEY (meal_template_id) REFERENCES meal_templates(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS meal_template_slot_dishes (
    quanty SMALLINT NOT NULL DEFAULT 0 CHECK (quanty >= 0),
    meal_template_slot_id UUID NOT NULL,
    dish_id UUID NOT NULL,
    FOREIGN KEY (dish_id) REFERENCES dishes(id) ON DELETE CASCADE,
    FOREIGN KEY (meal_template_slot_id) REFERENCES meal_template_slots(id) ON DELETE CASCADE,
    PRIMARY KEY(meal_template_slot_id, dish_id)
);

CREATE TABLE IF NOT EXISTS meal_template_tags (
    tag_id UUID,
    meal_template_id UUID,
    FOREIGN KEY (meal_template_id) REFERENCES meal_templates(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE RESTRICT,
    PRIMARY KEY(meal_template_id, tag_id)
);

CREATE TABLE IF NOT EXISTS notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    remind_training BOOLEAN NOT NULL DEFAULT TRUE,
    remind_meals BOOLEAN NOT NULL DEFAULT TRUE,
    remind_water BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_mode_start TIME,
    quiet_mode_end TIME,
    user_id UUID NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ai_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    role VARCHAR(15) NOT NULL CHECK (role IN ('пользователь', 'ассистент')),
    message_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS weather_caches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    temperature NUMERIC(4, 1) NOT NULL,
    city_name VARCHAR(100) NOT NULL,
    weather_condition VARCHAR(100) NOT NULL,
    precipitation_probability SMALLINT NOT NULL DEFAULT 0 CHECK (precipitation_probability BETWEEN 0 AND 100),
    temperature_night NUMERIC(4, 1) NOT NULL,
    UNIQUE(city_name, date)
);

CREATE TABLE IF NOT EXISTS weather_cache_per_days (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    time TIME,
    temperature NUMERIC(4, 1) NOT NULL,
    city_name VARCHAR(100) NOT NULL,
    weather_condition VARCHAR(100) NOT NULL,
    precipitation_probability SMALLINT NOT NULL DEFAULT 0 CHECK (precipitation_probability BETWEEN 0 AND 100),
    temperature_night NUMERIC(4, 1) NOT NULL,
    weather_cache_id UUID REFERENCES weather_caches(id) ON DELETE CASCADE,
    UNIQUE(weather_cache_id, time)
);

CREATE TABLE IF NOT EXISTS sync_metadatas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    table_name VARCHAR(50) NOT NULL,
    record_id UUID NOT NULL,
    operation_type VARCHAR(10) NOT NULL CHECK (operation_type IN ('INSERT', 'UPDATE', 'DELETE')),
    client_updated_at TIMESTAMPTZ NOT NULL,
    is_synced BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    ip_address INET,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE
);