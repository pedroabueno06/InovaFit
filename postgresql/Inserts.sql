INSERT INTO aluno(nome,email,data_nascimento,data_cadastro)
    VALUES('Pedro Ascenço Bueno', 'aluno1@exemplo.com',
    DATE '2006-05-15', CURRENT_DATE-INTERVAL '14 months'),

    ('Joaquim Attanasio da Silva', 'aluno2@exemplo.com',
    DATE '2006-06-16', CURRENT_DATE-INTERVAL '3 months'),

    ('Gabriel Gastal', 'aluno3@exemplo.com',
    DATE '2005-07-17', CURRENT_DATE-INTERVAL '16 months'),

    ('Fabrício Lopes', 'aluno4@exemplo.com',
    DATE '2007-08-18', CURRENT_DATE-INTERVAL '2 months');

INSERT INTO  plano(nome,valor_mensal,duracao_meses)
    VALUES('Mensal', 120, 1),
          ('Trimestral', 110, 3),
          ('Anual', 95, 12);

INSERT INTO modalidade(nome,descricao)
    VALUES('Musculação', 'Treinamento de força'),
          ('Spinning', 'Aula de ciclismo indoor'),
          ('Pilates', 'Condicionamento e postura'),
          ('Funcional', 'Treinamento funcional');

INSERT INTO matricula(id_aluno,id_plano,data_inicio,status,bloqueada)
    VALUES(1, 3, CURRENT_DATE-INTERVAL '14 months', 'ATIVA', FALSE),
          (2, 1, CURRENT_DATE-INTERVAL '3 months', 'ATIVA', FALSE),
          (3, 2, CURRENT_DATE-INTERVAL '16 months', 'ATIVA', FALSE),
          (4, 1, CURRENT_DATE-INTERVAL '2 months', 'CANCELADA', FALSE);

INSERT INTO matricula_modalidade(id_matricula,id_modalidade)
    VALUES(1, 1),
          (1, 2),
          (2, 1),
          (3, 1),
          (3, 3);

INSERT INTO pagamento(id_matricula,competencia,vencimento,data_pagamento,valor,status)
    VALUES(1, date_trunc('month', CURRENT_DATE)::date, CURRENT_DATE-1, CURRENT_DATE-2, 85.50, 'PAGO'),
          (2, date_trunc('month', CURRENT_DATE)::date, CURRENT_DATE+5, NULL, 120, 'PENDENTE'),
          (3, date_trunc('month', CURRENT_DATE)::date, CURRENT_DATE-12, NULL, 110, 'ATRASADO');
