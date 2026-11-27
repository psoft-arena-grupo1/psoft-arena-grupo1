package psoft.arena.model;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;

final class ValidadorCodigoJava {

    private static final List<String> OPCOES = List.of("--release", "17", "-proc:none");
    private static final String NOME_ARQUIVO = "SolucaoSubmetidaPsoftArena";

    private ValidadorCodigoJava() {
    }

    static void validar(String codigoFonte) {
        JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
        if (compilador == null) {
            throw new IllegalStateException("A validação de código Java requer a execução em um JDK.");
        }
        DiagnosticCollector<JavaFileObject> diagnosticos = new DiagnosticCollector<>();
        JavacTask tarefa = (JavacTask) compilador.getTask(null, null, diagnosticos, OPCOES, null,
                List.of(new CodigoEmMemoria(codigoFonte)));
        boolean declaraTipo;
        try {
            declaraTipo = declaraTipo(tarefa.parse());
        } catch (Exception e) {
            throw new IllegalArgumentException("A solução deve ser submetida em Java.");
        }
        Optional<Diagnostic<? extends JavaFileObject>> erro = diagnosticos.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .findFirst();
        if (!declaraTipo) {
            throw new IllegalArgumentException("A solução deve ser submetida em Java.");
        }
        if (erro.isPresent()) {
            Diagnostic<? extends JavaFileObject> d = erro.get();
            throw new IllegalArgumentException("O código Java submetido possui erro de sintaxe na linha "
                    + d.getLineNumber() + ": " + d.getMessage(Locale.getDefault()));
        }
    }

    private static boolean declaraTipo(Iterable<? extends CompilationUnitTree> unidades) {
        for (CompilationUnitTree unidade : unidades) {
            if (unidade.getTypeDecls().stream()
                    .filter(ClassTree.class::isInstance)
                    .map(ClassTree.class::cast)
                    .anyMatch(tipo -> !tipo.getSimpleName().contentEquals(NOME_ARQUIVO))) {
                return true;
            }
        }
        return false;
    }

    private static final class CodigoEmMemoria extends SimpleJavaFileObject {

        private final String codigo;

        CodigoEmMemoria(String codigo) {
            super(URI.create("string:///" + NOME_ARQUIVO + ".java"), Kind.SOURCE);
            this.codigo = codigo;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return codigo;
        }
    }
}
