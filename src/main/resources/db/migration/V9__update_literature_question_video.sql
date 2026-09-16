UPDATE lesson_section_questions question
SET youtube_title = 'Sự sống và cái chết - Ngữ văn 10',
    youtube_url = 'https://www.youtube.com/watch?v=hgoeo2Cb7Vw'
FROM lesson_sections section
JOIN lessons lesson ON lesson.id = section.lesson_id
JOIN subjects subject ON subject.id = lesson.subject_id
WHERE question.section_id = section.id
  AND subject.code = 'LITERATURE';
