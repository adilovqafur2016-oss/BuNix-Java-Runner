import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Java_Pro_class_runner extends JFrame {
    
    private JTextArea editor;
    private JTextArea outputArea;
    private JLabel statusLabel;
    private JLabel fileLabel;
    private JButton openButton;
    private JButton saveButton;
    private JButton compileButton;
    private JButton runButton;
    private JButton projectButton;
    private JButton libraryButton;
    private JButton clearButton;
    
    private Path currentFile;
    private Path projectDir;
    private Path compiledDir;
    private Path librariesDir;
    
    private static final Pattern PACKAGE_PATTERN = 
        Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    
    private static final Pattern CLASS_PATTERN = 
        Pattern.compile("(?:public\\s+)?(?:final\\s+|abstract\\s+)?class\\s+(\\w+)", Pattern.MULTILINE);
    
    // Dark theme colors
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color BG_PANEL = new Color(40, 40, 40);
    private static final Color BG_EDITOR = new Color(25, 25, 25);
    private static final Color FG_TEXT = new Color(220, 220, 220);
    private static final Color FG_DIM = new Color(160, 160, 160);
    private static final Color ACCENT = new Color(70, 130, 180);
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public Java_Pro_class_runner() {
        setTitle("BuNix Java Runner — Google is malware");
        setSize(1100, 700);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        applyDarkTheme();
        buildUI();
    }
    
    private void applyDarkTheme() {
        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("Label.foreground", FG_TEXT);
        UIManager.put("Button.background", new Color(55, 55, 55));
        UIManager.put("Button.foreground", FG_TEXT);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("TextArea.background", BG_EDITOR);
        UIManager.put("TextArea.foreground", FG_TEXT);
        UIManager.put("TextArea.caretForeground", Color.WHITE);
        UIManager.put("TextArea.selectionBackground", ACCENT);
        UIManager.put("TextArea.selectionForeground", Color.WHITE);
        UIManager.put("ScrollPane.background", BG_PANEL);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDER_COLOR));
        UIManager.put("SplitPane.background", BG_DARK);
        UIManager.put("SplitPane.dividerSize", 6);
        UIManager.put("TitledBorder.titleColor", FG_DIM);
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", FG_TEXT);
        UIManager.put("FileChooser.background", BG_PANEL);
    }
    
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // TOP toolbar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setBackground(BG_DARK);
        
        openButton = createButton("Open");
        openButton.addActionListener(e -> openFile());
        
        saveButton = createButton("Save");
        saveButton.addActionListener(e -> saveFile());
        
        projectButton = createButton("Project");
        projectButton.addActionListener(e -> selectProject());
        
        libraryButton = createButton("Add JAR");
        libraryButton.addActionListener(e -> addLibrary());
        
        compileButton = createButton("Compile");
        compileButton.addActionListener(e -> compile());
        
        runButton = createButton("Run");
        runButton.addActionListener(e -> run());
        
        clearButton = createButton("Clear Output");
        clearButton.addActionListener(e -> outputArea.setText(""));
        
        top.add(openButton);
        top.add(saveButton);
        top.add(projectButton);
        top.add(libraryButton);
        top.add(compileButton);
        top.add(runButton);
        top.add(clearButton);
        
        fileLabel = new JLabel("No file");
        fileLabel.setForeground(FG_DIM);
        fileLabel.setBorder(new EmptyBorder(0, 12, 0, 0));
        top.add(fileLabel);
        
        JLabel warningLabel = new JLabel("  |  Google is malware — use another mail");
        warningLabel.setForeground(new Color(220, 80, 80));
        warningLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        top.add(warningLabel);
        
        root.add(top, BorderLayout.NORTH);
        
        // Editor
        editor = new JTextArea();
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        editor.setBackground(BG_EDITOR);
        editor.setForeground(FG_TEXT);
        editor.setCaretColor(Color.WHITE);
        editor.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        editor.setTabSize(4);
        
        JScrollPane editorScroll = new JScrollPane(editor);
        editorScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), "Editor"));
        editorScroll.getViewport().setBackground(BG_EDITOR);
        
        // Output
        outputArea = new JTextArea();
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        outputArea.setEditable(false);
        outputArea.setBackground(BG_EDITOR);
        outputArea.setForeground(FG_TEXT);
        outputArea.setCaretColor(Color.WHITE);
        outputArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), "Output"));
        outputScroll.setPreferredSize(new Dimension(0, 180));
        outputScroll.getViewport().setBackground(BG_EDITOR);
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorScroll, outputScroll);
        split.setResizeWeight(0.72);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        
        root.add(split, BorderLayout.CENTER);
        
        // Status
        statusLabel = new JLabel("Ready — Google is malware");
        statusLabel.setForeground(FG_DIM);
        statusLabel.setBorder(new EmptyBorder(4, 2, 0, 0));
        root.add(statusLabel, BorderLayout.SOUTH);
        
        setContentPane(root);
        getContentPane().setBackground(BG_DARK);
        
        // Ctrl+S shortcut
        editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        editor.getActionMap().put("save", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveFile();
            }
        });
    }
    
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(55, 55, 55));
        btn.setForeground(FG_TEXT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return btn;
    }
    
    private void setBusy(boolean busy) {
        SwingUtilities.invokeLater(() -> {
            openButton.setEnabled(!busy);
            saveButton.setEnabled(!busy);
            projectButton.setEnabled(!busy);
            libraryButton.setEnabled(!busy);
            compileButton.setEnabled(!busy);
            runButton.setEnabled(!busy);
            clearButton.setEnabled(!busy);
        });
    }
    
    // ------------------------------------------------------------
    // FILE OPERATIONS
    // ------------------------------------------------------------
    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (projectDir != null) chooser.setCurrentDirectory(projectDir.toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Java (*.java)", "java"));
        
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        currentFile = chooser.getSelectedFile().toPath();
        try {
            String content = Files.readString(currentFile, StandardCharsets.UTF_8);
            editor.setText(content);
            fileLabel.setText(currentFile.getFileName().toString());
            status("Opened: " + currentFile.getFileName());
            
            if (projectDir == null) {
                projectDir = currentFile.getParent();
                setupDirs();
            }
        } catch (IOException e) {
            showError("Cannot open file:\n" + e.getMessage());
        }
    }
    
    private void saveFile() {
        if (currentFile == null) {
            saveAs();
            return;
        }
        try {
            Files.writeString(currentFile, editor.getText(), StandardCharsets.UTF_8);
            status("Saved: " + currentFile.getFileName());
        } catch (IOException e) {
            showError("Cannot save:\n" + e.getMessage());
        }
    }
    
    private void saveAs() {
        JFileChooser chooser = new JFileChooser();
        if (projectDir != null) chooser.setCurrentDirectory(projectDir.toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Java (*.java)", "java"));
        
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        currentFile = chooser.getSelectedFile().toPath();
        if (!currentFile.toString().toLowerCase().endsWith(".java")) {
            currentFile = Paths.get(currentFile.toString() + ".java");
        }
        
        try {
            Files.writeString(currentFile, editor.getText(), StandardCharsets.UTF_8);
            fileLabel.setText(currentFile.getFileName().toString());
            status("Saved as: " + currentFile.getFileName());
            
            if (projectDir == null) {
                projectDir = currentFile.getParent();
                setupDirs();
            }
        } catch (IOException e) {
            showError("Cannot save:\n" + e.getMessage());
        }
    }
    
    // ------------------------------------------------------------
    // PROJECT + LIBRARIES
    // ------------------------------------------------------------
    private void selectProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        projectDir = chooser.getSelectedFile().toPath();
        setupDirs();
        status("Project: " + projectDir);
        log("Project set: " + projectDir);
    }
    
    private void setupDirs() {
        if (projectDir == null) return;
        compiledDir = projectDir.resolve("compiled");
        librariesDir = projectDir.resolve("libraries");
        try {
            Files.createDirectories(compiledDir);
            Files.createDirectories(librariesDir);
        } catch (IOException e) {
            showError("Cannot create folders:\n" + e.getMessage());
        }
    }
    
    private void addLibrary() {
        if (projectDir == null) {
            JOptionPane.showMessageDialog(this, "First select a project folder.", 
                "No Project", JOptionPane.WARNING_MESSAGE);
            return;
        }
        setupDirs();
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JAR (*.jar)", "jar"));
        
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        Path src = chooser.getSelectedFile().toPath();
        Path dest = librariesDir.resolve(src.getFileName());
        
        try {
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            log("Library added: " + dest.getFileName());
            status("Library added");
        } catch (IOException e) {
            showError("Cannot copy JAR:\n" + e.getMessage());
        }
    }
    
    // ------------------------------------------------------------
    // COMPILE
    // ------------------------------------------------------------
    private void compile() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Open or save a .java file first.", 
                "No File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (projectDir == null) {
            projectDir = currentFile.getParent();
            setupDirs();
        }
        
        // Auto-save before compile
        try {
            Files.writeString(currentFile, editor.getText(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            showError("Cannot auto-save before compile:\n" + e.getMessage());
            return;
        }
        
        setBusy(true);
        
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    status("Compiling...");
                    log("");
                    log("=== COMPILE ===");
                });
                
                boolean ok = doCompile();
                SwingUtilities.invokeLater(() -> status(ok ? "Compiled" : "Compile failed"));
            } finally {
                setBusy(false);
            }
        }).start();
    }
    
    private boolean doCompile() {
        List<String> cmd = new ArrayList<>();
        cmd.add("javac");
        cmd.add("-encoding");
        cmd.add("UTF-8");
        cmd.add("-cp");
        cmd.add(buildClassPath());
        cmd.add("-d");
        cmd.add(compiledDir.toAbsolutePath().toString());
        cmd.add(currentFile.toAbsolutePath().toString());
        
        log("$ " + String.join(" ", cmd));
        
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    final String out = line;
                    SwingUtilities.invokeLater(() -> log(out));
                }
            }
            
            int code = p.waitFor();
            if (code == 0) {
                log("Compilation successful.");
                return true;
            } else {
                log("Compilation failed. Exit: " + code);
                return false;
            }
        } catch (Exception e) {
            log("Compiler error: " + e.getMessage());
            return false;
        }
    }
    
    // ------------------------------------------------------------
    // RUN
    // ------------------------------------------------------------
    private void run() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Open a .java file first.", 
                "No File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (projectDir == null) {
            projectDir = currentFile.getParent();
            setupDirs();
        }
        
        // Auto-save
        try {
            Files.writeString(currentFile, editor.getText(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            showError("Cannot auto-save before run:\n" + e.getMessage());
            return;
        }
        
        setBusy(true);
        
        new Thread(() -> {
            try {
                Path classFile = getExpectedClassFile();
                if (!Files.exists(classFile)) {
                    log("No .class found, compiling first...");
                    if (!doCompile()) {
                        SwingUtilities.invokeLater(() -> status("Compile failed"));
                        return;
                    }
                }
                
                String mainClass = getFullyQualifiedName();
                if (mainClass == null || mainClass.isBlank()) {
                    log("ERROR: Cannot determine main class name. Make sure there is a class declaration.");
                    SwingUtilities.invokeLater(() -> status("Error"));
                    return;
                }
                
                List<String> cmd = new ArrayList<>();
                cmd.add("java");
                cmd.add("-cp");
                cmd.add(buildClassPath());
                cmd.add(mainClass);
                
                SwingUtilities.invokeLater(() -> {
                    status("Running...");
                    log("");
                    log("=== RUN ===");
                    log("$ " + String.join(" ", cmd));
                });
                
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(projectDir.toFile());
                pb.redirectErrorStream(true);
                Process p = pb.start();
                
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        final String out = line;
                        SwingUtilities.invokeLater(() -> log(out));
                    }
                }
                
                int code = p.waitFor();
                SwingUtilities.invokeLater(() -> {
                    log("Finished. Exit code: " + code);
                    status("Finished");
                });
            } catch (Exception e) {
                log("Run error: " + e.getMessage());
                SwingUtilities.invokeLater(() -> status("Error"));
            } finally {
                setBusy(false);
            }
        }).start();
    }
    
    // ------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------
    private String buildClassPath() {
        List<String> entries = new ArrayList<>();
        if (compiledDir != null) {
            entries.add(compiledDir.toAbsolutePath().toString());
        }
        
        if (librariesDir != null && Files.exists(librariesDir)) {
            try (var stream = Files.list(librariesDir)) {
                stream.filter(p -> p.toString().toLowerCase().endsWith(".jar"))
                      .forEach(p -> entries.add(p.toAbsolutePath().toString()));
            } catch (IOException ignored) {}
        }
        
        return String.join(File.pathSeparator, entries);
    }
    
    private String readPackage() {
        String content = editor.getText();
        Matcher m = PACKAGE_PATTERN.matcher(content);
        if (m.find()) return m.group(1).trim();
        return null;
    }
    
    private String readClassName() {
        String content = editor.getText();
        Matcher m = CLASS_PATTERN.matcher(content);
        if (m.find()) return m.group(1).trim();
        return null;
    }
    
    private String getSimpleClassName() {
        // Prefer actual class name from source code
        String fromSource = readClassName();
        if (fromSource != null && !fromSource.isBlank()) {
            return fromSource;
        }
        
        // Fallback to filename
        if (currentFile != null) {
            String name = currentFile.getFileName().toString();
            if (name.endsWith(".java")) {
                name = name.substring(0, name.length() - 5);
            }
            if (!name.isBlank()) return name;
        }
        
        return null;
    }
    
    private String getFullyQualifiedName() {
        String pkg = readPackage();
        String simple = getSimpleClassName();
        if (simple == null || simple.isBlank()) return null;
        return (pkg == null || pkg.isBlank()) ? simple : pkg + "." + simple;
    }
    
    private Path getExpectedClassFile() {
        String pkg = readPackage();
        String simple = getSimpleClassName();
        
        if (simple == null || simple.isBlank()) {
            return compiledDir.resolve("Unknown.class");
        }
        
        if (pkg == null || pkg.isBlank()) {
            return compiledDir.resolve(simple + ".class");
        }
        
        Path dir = compiledDir;
        for (String part : pkg.split("\\.")) {
            dir = dir.resolve(part);
        }
        return dir.resolve(simple + ".class");
    }
    
    private void log(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }
    
    private void status(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }
    
    private void showError(String msg) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new Java_Pro_class_runner().setVisible(true);
        });
    }
}
