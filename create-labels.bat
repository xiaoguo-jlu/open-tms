@echo off
rem 创建角色标签
"C:\Program Files\GitHub CLI\gh.exe" label create PM --repo xiaoguo-jlu/open-tms --color "1D76DB" --description "Product Manager" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create TA --repo xiaoguo-jlu/open-tms --color "F88" --description "Technical Architect" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create Dev --repo xiaoguo-jlu/open-tms --color "84B6EB" --description "Developer" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create QA --repo xiaoguo-jlu/open-tms --color "E4E669" --description "QA Engineer" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create UX --repo xiaoguo-jlu/open-tms --color "D4C5F9" --description "UX Designer" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create PM-Lead --repo xiaoguo-jlu/open-tms --color "0E8A16" --description "Project Lead" 2>nul

rem 创建类型标签
"C:\Program Files\GitHub CLI\gh.exe" label create Feature --repo xiaoguo-jlu/open-tms --color "0E8A16" --description "New Feature" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create US --repo xiaoguo-jlu/open-tms --color "D4C5F9" --description "User Story" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create Task --repo xiaoguo-jlu/open-tms --color "3D4F9F" --description "Technical Task" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create Bug --repo xiaoguo-jlu/open-tms --color "CB2431" --description "Bug" 2>nul
"C:\Program Files\GitHub CLI\gh.exe" label create 技术债务 --repo xiaoguo-jlu/open-tms --color "FBCA04" --description "Technical Debt" 2>nul

echo Labels created successfully!