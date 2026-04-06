Заупстить main -> help -> Далее мы увидим все доступные действия:

assign-role               - Назначить роль пользователю  
assignment-active         - Активные назначения  
assignment-expired        - Истёкшие временные назначения  
assignment-extend         - Продлить временное назначение  
assignment-list           - Список всех назначений  
assignment-list-role      - Список пользователей с конкретной ролью  
assignment-list-user      - Назначения конкретного пользователя  
assignment-search         - Поиск назначений по фильтрам  
clear                     - Очистить экран  
exit                      - Выход из программы  
help                      - Справка по командам  
load                      - Загрузить данные из файла  
permissions-check         - Проверить наличие права у пользователя  
permissions-user          - Все права конкретного пользователя  
revoke-role               - Отозвать роль у пользователя  
role-add-permission       - Добавить право к роли  
role-create               - Создать новую роль  
role-delete               - Удалить роль  
role-list                 - Вывести список всех ролей  
role-remove-permission    - Удалить право из роли  
role-search               - Поиск ролей  
role-update               - Обновить роль  
role-view                 - Просмотр роли  
save                      - Сохранить данные в файл  
stats                     - Статистика системы  
user-create               - Создать нового пользователя  
user-delete               - Удалить пользователя  
user-list                 - Вывести список всех пользователей  
user-search               - Поиск пользователей по фильтрам  
user-update               - Обновить данные пользователя  
user-view                 - Просмотр информации о пользователе  


## Основные действия:

### Пользователи
#### user-list, user-create, user-view, user-delete  
Управление учётными записями  

### Роли
#### role-list, role-create, role-delete, role-view  
Управление ролями и правами  

### Назначения
#### assign-role, revoke-role, assignment-list, assignment-active  
Привязка ролей к пользователям  

### Отчёты
#### report-users, report-roles, report-matrix  
Генерация ASCII-отчётов  

### Аудит
#### audit-log  
Просмотр и сохранение лога действий  

### Асинхронные
#### report-users-async, save-async, log-async  
Фоновое выполнение без блокировки UI  

### Планировщик
#### scheduler-start <сек>, scheduler-stop, scheduler-status  
Автоочистка истёкших назначений и лог статистики  

### Сервисные
#### help, stats, clear, exit  
Справка, статистика, выход  