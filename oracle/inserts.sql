INSERT INTO aluno(nome,email,data_nascimento,data_cadastro)
    VALUES('Pedro Ascenço Bueno', 'pedro.ascenco@gmail.com',
    DATE '2006-05-15', ADD_MONTHS(TRUNC(SYSDATE), -14));

INSERT INTO aluno(nome,email,data_nascimento,data_cadastro)
    VALUES('Joaquim Attanasio da Silva', 'jattanasio57@gmail.com',
    DATE '2006-06-16', ADD_MONTHS(TRUNC(SYSDATE), -3));

INSERT INTO aluno(nome,email,data_nascimento,data_cadastro)
    VALUES('Gabriel Gastal', 'gabrielgastal2017@gmail.com',
    DATE '2005-07-17', ADD_MONTHS(TRUNC(SYSDATE), -16));

INSERT INTO aluno(nome,email,data_nascimento,data_cadastro)
    VALUES('Fabrício Lopes', 'fabrilopes403@gmail.com',
    DATE '2007-08-18', ADD_MONTHS(TRUNC(SYSDATE), -2));

INSERT INTO plano(nome,valor_mensal,duracao_meses)
    VALUES('Mensal', 120, 1);

INSERT INTO plano(nome,valor_mensal,duracao_meses)
    VALUES('Trimestral', 110, 3);

INSERT INTO plano(nome,valor_mensal,duracao_meses)
    VALUES('Anual', 95, 12);

INSERT INTO modalidade(nome,descricao)
    VALUES('Musculação', 'Treinamento de força');

INSERT INTO modalidade(nome,descricao)
    VALUES('Spinning', 'Aula de ciclismo indoor');

INSERT INTO modalidade(nome,descricao)
    VALUES('Pilates', 'Condicionamento e postura');

INSERT INTO modalidade(nome,descricao)
    VALUES('Funcional', 'Treinamento funcional');

INSERT INTO matricula(id_aluno,id_plano,data_inicio,status,bloqueada)
    VALUES(1, 3, ADD_MONTHS(TRUNC(SYSDATE), -14), 'ATIVA', 0);

INSERT INTO matricula(id_aluno,id_plano,data_inicio,status,bloqueada)
    VALUES(2, 1, ADD_MONTHS(TRUNC(SYSDATE), -3), 'ATIVA', 0);

INSERT INTO matricula(id_aluno,id_plano,data_inicio,status,bloqueada)
    VALUES(3, 2, ADD_MONTHS(TRUNC(SYSDATE), -16), 'ATIVA', 0);

INSERT INTO matricula(id_aluno,id_plano,data_inicio,status,bloqueada)
    VALUES(4, 1, ADD_MONTHS(TRUNC(SYSDATE), -2), 'CANCELADA', 0);

INSERT INTO matricula_modalidade(id_matricula,id_modalidade)
    VALUES(1,1);

INSERT INTO matricula_modalidade(id_matricula,id_modalidade)
    VALUES(1,2);

INSERT INTO matricula_modalidade(id_matricula,id_modalidade)
    VALUES(2,1);

INSERT INTO matricula_modalidade(id_matricula,id_modalidade)
    VALUES(3,1);

INSERT INTO matricula_modalidade(id_matricula,id_modalidade)
    VALUES(3,3);

INSERT INTO pagamento(id_matricula,competencia,vencimento,data_pagamento,valor,status)
    VALUES(1, TRUNC(SYSDATE, 'MM'),
           TRUNC(SYSDATE) -1,
           TRUNC(SYSDATE) -2, 85.50, 'PAGO');

INSERT INTO pagamento(id_matricula,competencia, vencimento, valor, status)
    VALUES(2, TRUNC(SYSDATE, 'MM'),
           TRUNC(SYSDATE) +5, 120, 'PENDENTE');

INSERT INTO pagamento(id_matricula,competencia, vencimento, valor, status)
    VALUES(3, TRUNC(SYSDATE, 'MM'),
           TRUNC(SYSDATE) -12, 110, 'ATRASADO');

COMMIT;