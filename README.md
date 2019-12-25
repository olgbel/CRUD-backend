Регистрация
curl -v -H "Content-Type: application/json" -X POST -d  "@loginData.json" http://localhost:9999/api/v1/registration

Аутентификация, выдача токена
curl -v -H "Content-Type: application/json" -X POST -d  "@loginData.json" http://localhost:9999/api/v1/authentication

GET всех постов
curl -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6Mn0.MTwiw7lL_X_KZyw84vBBcePuJwo0SXwvH1ipjl5aIVY" http://localhost:9999/api/v1/posts

DELETE поста
curl -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6Mn0.MTwiw7lL_X_KZyw84vBBcePuJwo0SXwvH1ipjl5aIVY" -v -X DELETE http://localhost:9999/api/v1/posts/2

GET определённого поста
curl -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6Mn0.MTwiw7lL_X_KZyw84vBBcePuJwo0SXwvH1ipjl5aIVY" http://localhost:9999/api/v1/posts/1

CREATE поста
curl -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6Mn0.MTwiw7lL_X_KZyw84vBBcePuJwo0SXwvH1ipjl5aIVY" -v -H "Content-Type: application/json" -X POST -d  "@data.json" http://localhost:9999/api/v1/posts

UPDATE POST
curl -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6Mn0.MTwiw7lL_X_KZyw84vBBcePuJwo0SXwvH1ipjl5aIVY" -H "Content-Type: application/json" -X PUT -d "@data.json"  http://localhost:9999/api/v1/posts/1

REPOST
curl -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6Mn0.MTwiw7lL_X_KZyw84vBBcePuJwo0SXwvH1ipjl5aIVY" -v -H "Content-Type: application/json" -X POST -d  "@data.json" http://localhost:9999/api/v1/posts/repost/1
