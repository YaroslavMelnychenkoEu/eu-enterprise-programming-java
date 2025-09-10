import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/* ===================== ВИНЯТКИ ===================== */
class FSException extends RuntimeException { public FSException(String m){super(m);} }
class NotFoundException extends FSException { public NotFoundException(String m){super(m);} }
class AlreadyExistsException extends FSException { public AlreadyExistsException(String m){super(m);} }
class PermissionDeniedException extends FSException { public PermissionDeniedException(String m){super(m);} }
class InvalidOperationException extends FSException { public InvalidOperationException(String m){super(m);} }

/* ===================== ПРАВА/КОРИСТУВАЧ ===================== */
enum Permission { READ, WRITE, EXECUTE }

class User {
    private final String name;
    public User(String name) {
        if (name==null || name.isBlank()) throw new IllegalArgumentException("User name порожній");
        this.name = name.trim();
    }
    public String getName(){ return name; }
    @Override public String toString(){ return name; }
}

/* ===================== МОДЕЛІ ФАЙЛОВОЇ СИСТЕМИ ===================== */
class VFile {
    private final String name;
    private final long size;             // байт
    private final String type;           // розширення без крапки, напр. "txt"
    private final LocalDateTime created;
    private byte[] content;              // для простоти — байтовий вміст (може бути null)

    public VFile(String name, long size, String type, LocalDateTime created, byte[] content) {
        if (name==null || name.isBlank()) throw new IllegalArgumentException("File name порожній");
        if (size < 0) throw new IllegalArgumentException("size < 0");
        if (type==null) type = "";
        this.name = name.trim();
        this.size = size;
        this.type = type.trim().toLowerCase();
        this.created = created == null ? LocalDateTime.now() : created;
        this.content = content;
    }
    public String getName(){ return name; }
    public long getSize(){ return size; }
    public String getType(){ return type; }
    public LocalDateTime getCreated(){ return created; }
    public byte[] getContent(){ return content; }

    @Override public String toString() {
        return String.format("VFile{name='%s', size=%d, type='%s', created=%s}", name, size, type, created);
    }
}

class Directory {
    private final String name;
    private final Directory parent;  // null для root
    private final Map<String, Directory> subdirs = new LinkedHashMap<>();
    private final Map<String, VFile> files = new LinkedHashMap<>();
    private final Map<String, EnumSet<Permission>> acl = new HashMap<>(); // userName -> perms

    public Directory(String name, Directory parent) {
        if (parent != null && (name == null || name.isBlank()))
            throw new IllegalArgumentException("Directory name порожній");
        this.name = (name == null) ? "" : name.trim();
        this.parent = parent;
    }

    public String getName(){ return name; }
    public Directory getParent(){ return parent; }
    public Collection<Directory> getSubdirs(){ return Collections.unmodifiableCollection(subdirs.values()); }
    public Collection<VFile> getFiles(){ return Collections.unmodifiableCollection(files.values()); }

    public String absolutePath() {
        if (parent == null) return "/";
        Deque<String> parts = new ArrayDeque<>();
        Directory cur = this;
        while (cur != null && cur.parent != null) { parts.addFirst(cur.name); cur = cur.parent; }
        return "/" + String.join("/", parts);
    }

    /* ACL */
    public void grant(User user, Permission... perms) {
        acl.computeIfAbsent(user.getName(), k -> EnumSet.noneOf(Permission.class)).addAll(Arrays.asList(perms));
    }
    public boolean has(User user, Permission p) {
        EnumSet<Permission> set = acl.get(user.getName());
        return set != null && set.contains(p);
    }

    /* Файли */
    public void addFile(VFile f) {
        if (files.containsKey(f.getName())) throw new AlreadyExistsException("Файл вже існує: " + f.getName());
        files.put(f.getName(), f);
    }
    public VFile getFile(String name) {
        VFile f = files.get(name);
        if (f==null) throw new NotFoundException("Файл не знайдено: " + name);
        return f;
    }
    public void removeFile(String name) {
        if (!files.containsKey(name)) throw new NotFoundException("Файл не знайдено: " + name);
        files.remove(name);
    }

    /* Директорії */
    public void addDir(Directory d) {
        if (subdirs.containsKey(d.getName())) throw new AlreadyExistsException("Директорія вже існує: " + d.getName());
        subdirs.put(d.getName(), d);
    }
    public Directory getDir(String name) {
        Directory d = subdirs.get(name);
        if (d==null) throw new NotFoundException("Директорію не знайдено: " + name);
        return d;
    }
    public void removeDir(String name) {
        if (!subdirs.containsKey(name)) throw new NotFoundException("Директорію не знайдено: " + name);
        subdirs.remove(name);
    }

    @Override public String toString() {
        return String.format("Directory{%s, files=%d, subdirs=%d}", absolutePath(), files.size(), subdirs.size());
    }
}

/* ===================== ФАЙЛОВА СИСТЕМА ===================== */
class FileSystem {
    private final Directory root;
    private final User admin;

    public FileSystem(User admin) {
        this.admin = admin;
        this.root = new Directory("", null); // ім'я "" для root
        // Стандартні права: адміну – все, іншим – лише READ на корінь
        root.grant(admin, Permission.READ, Permission.WRITE, Permission.EXECUTE);
    }

    public Directory getRoot(){ return root; }

    /* ---- API ---- */
    public Directory mkdir(User user, String path) {
        PathParts pp = splitPath(path);
        Directory parent = resolveDir(user, pp.parentPath, Permission.WRITE);
        String name = pp.name;
        Directory d = new Directory(name, parent);
        parent.addDir(d);
        if (parent.has(user, Permission.WRITE)) d.grant(user, Permission.READ, Permission.WRITE, Permission.EXECUTE);
        return d;
    }

    public VFile createFile(User user, String dirPath, String fileName, long size, String ext, byte[] content) {
        Directory dir = resolveDir(user, dirPath, Permission.WRITE);
        VFile f = new VFile(fileName, size, ext, LocalDateTime.now(), content);
        dir.addFile(f);
        return f;
    }

    public void deleteFile(User user, String filePath) {
        PathParts pp = splitPath(filePath);
        Directory dir = resolveDir(user, pp.parentPath, Permission.WRITE);
        dir.removeFile(pp.name);
    }

    public void deleteDir(User user, String dirPath) {
        if ("/".equals(dirPath)) throw new InvalidOperationException("Не можна видалити кореневу директорію");
        PathParts pp = splitPath(dirPath);
        Directory parent = resolveDir(user, pp.parentPath, Permission.WRITE);
        Directory target = parent.getDir(pp.name);
        if (!target.getFiles().isEmpty() || !target.getSubdirs().isEmpty())
            throw new InvalidOperationException("Директорія не порожня");
        parent.removeDir(pp.name);
    }

    public void move(User user, String srcPath, String destDirPath) {
        PathParts src = splitPath(srcPath);
        Directory srcParent = resolveDir(user, src.parentPath, Permission.WRITE);
        Directory destDir = resolveDir(user, destDirPath, Permission.WRITE);

        // Файл?
        if (hasFile(srcParent, src.name)) {
            VFile f = srcParent.getFile(src.name);
            if (hasFile(destDir, f.getName()) || hasDir(destDir, f.getName()))
                throw new AlreadyExistsException("Об'єкт з такою назвою вже існує в місці призначення");
            srcParent.removeFile(f.getName());
            destDir.addFile(f);
            return;
        }
        // Директорія?
        if (hasDir(srcParent, src.name)) {
            Directory d = srcParent.getDir(src.name);
            if (hasDir(destDir, d.getName()) || hasFile(destDir, d.getName()))
                throw new AlreadyExistsException("Об'єкт з такою назвою вже існує в місці призначення");
            // «Переміщення» реалізуємо як deep-copy + видалення оригіналу
            Directory copy = deepCopyDir(d, destDir);
            destDir.addDir(copy);
            srcParent.removeDir(d.getName());
            return;
        }
        throw new NotFoundException("Джерело не знайдено: " + srcPath);
    }

    public void copy(User user, String srcPath, String destDirPath) {
        Directory destDir = resolveDir(user, destDirPath, Permission.WRITE);
        NodeLookup n = lookup(srcPath);
        if (n.file != null) {
            if (hasFile(destDir, n.file.getName()) || hasDir(destDir, n.file.getName()))
                throw new AlreadyExistsException("Об'єкт з такою назвою вже існує в місці призначення");
            VFile copied = new VFile(n.file.getName(), n.file.getSize(), n.file.getType(),
                    LocalDateTime.now(), n.file.getContent());
            destDir.addFile(copied);
        } else if (n.dir != null) {
            if (hasDir(destDir, n.dir.getName()) || hasFile(destDir, n.dir.getName()))
                throw new AlreadyExistsException("Об'єкт з такою назвою вже існує в місці призначення");
            Directory copy = deepCopyDir(n.dir, destDir);
            destDir.addDir(copy);
        } else throw new NotFoundException("Джерело не знайдено: " + srcPath);
    }

    /* ---- Пошук/Фільтрація ---- */
    public List<VFile> search(User user, String startDirPath, String nameSubstring) {
        Directory start = resolveDir(user, startDirPath, Permission.READ);
        String q = nameSubstring == null ? "" : nameSubstring.toLowerCase();
        List<VFile> out = new ArrayList<>();
        dfsFiles(start, out);
        return out.stream().filter(f -> f.getName().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public List<VFile> filterByExtension(User user, String startDirPath, String ext) {
        Directory start = resolveDir(user, startDirPath, Permission.READ);
        String e = ext == null ? "" : ext.toLowerCase();
        List<VFile> out = new ArrayList<>();
        dfsFiles(start, out);
        return out.stream().filter(f -> f.getType().equals(e)).collect(Collectors.toList());
    }

    /* ---- Допоміжні ---- */
    private Directory resolveDir(User user, String path, Permission needed) {
        if (path == null || path.isBlank()) throw new NotFoundException("Порожній шлях");
        if (!path.startsWith("/")) throw new InvalidOperationException("Підтримуються лише абсолютні шляхи");

        // Спецвипадок: корінь
        if ("/".equals(path)) {
            if (!root.has(user, needed)) throw new PermissionDeniedException("Немає прав на /: " + needed);
            return root;
        }

        String[] parts = Arrays.stream(path.split("/")).filter(s -> !s.isBlank()).toArray(String[]::new);
        Directory cur = root;

        // Проходимо ієрархію; на проміжних — потрібен EXECUTE
        for (int i = 0; i < parts.length; i++) {
            Directory next = cur.getDir(parts[i]);

            if (i < parts.length - 1) {
                if (!next.has(user, Permission.EXECUTE))
                    throw new PermissionDeniedException("Немає EXECUTE на " + next.absolutePath());
            } else {
                // Фінальна директорія: перевіряємо потрібні права
                if (needed == Permission.READ && !next.has(user, Permission.READ))
                    throw new PermissionDeniedException("Немає READ на " + next.absolutePath());
                if (needed == Permission.WRITE && !next.has(user, Permission.WRITE))
                    throw new PermissionDeniedException("Немає WRITE на " + next.absolutePath());
            }
            cur = next;
        }
        return cur;
    }

    private NodeLookup lookup(String path) {
        if ("/".equals(path)) return new NodeLookup(root, null);
        PathParts pp = splitPath(path);
        Directory dir = resolveDir(admin, pp.parentPath, Permission.READ); // lookup від імені адміна
        VFile f = hasFile(dir, pp.name) ? dir.getFile(pp.name) : null;
        Directory d = hasDir(dir, pp.name) ? dir.getDir(pp.name) : null;
        return new NodeLookup(d, f);
    }

    private static boolean hasFile(Directory d, String name){ try { d.getFile(name); return true; } catch(Exception e){ return false; } }
    private static boolean hasDir(Directory d, String name){ try { d.getDir(name); return true; } catch(Exception e){ return false; } }

    private static void dfsFiles(Directory dir, List<VFile> out) {
        out.addAll(dir.getFiles());
        for (Directory sub : dir.getSubdirs()) dfsFiles(sub, out);
    }

    private static Directory deepCopyDir(Directory src, Directory newParent) {
        Directory copy = new Directory(src.getName(), newParent);
        for (VFile f : src.getFiles()) {
            VFile nf = new VFile(f.getName(), f.getSize(), f.getType(), f.getCreated(), f.getContent());
            copy.addFile(nf);
        }
        for (Directory child : src.getSubdirs()) {
            Directory childCopy = deepCopyDir(child, copy);
            copy.addDir(childCopy);
        }
        return copy;
    }

    private static class PathParts {
        final String parentPath;
        final String name;
        PathParts(String parentPath, String name){ this.parentPath=parentPath; this.name=name; }
    }
    private static PathParts splitPath(String path) {
        if (path==null || path.isBlank() || !path.startsWith("/")) throw new InvalidOperationException("Некоректний шлях: " + path);
        if ("/".equals(path)) throw new InvalidOperationException("Шлях '/' не містить імені");
        int last = path.lastIndexOf('/');
        String parent = (last==0) ? "/" : path.substring(0, last);
        String name = path.substring(last+1);
        if (name.isBlank()) throw new InvalidOperationException("Порожнє ім'я в шляху");
        return new PathParts(parent, name);
    }

    /** Внутрішній результат пошуку вузла по шляху */
    private static class NodeLookup {
        final Directory dir; // якщо шлях вказує на директорію
        final VFile file;    // якщо шлях вказує на файл
        NodeLookup(Directory dir, VFile file){ this.dir = dir; this.file = file; }
    }
}

/* ===================== ДЕМО ===================== */
public class FileManagerDemo {
    public static void main(String[] args) {
        User admin = new User("admin");
        User bob   = new User("bob");

        FileSystem fs = new FileSystem(admin);

        // Створимо структуру /docs та /media
        Directory docs  = fs.mkdir(admin, "/docs");
        Directory media = fs.mkdir(admin, "/media");

        // Дамо Bob'у права на /docs (READ/WRITE/EXECUTE)
        docs.grant(bob, Permission.READ, Permission.WRITE, Permission.EXECUTE);

        // Файли
        fs.createFile(admin, "/docs", "readme.txt", 1200, "txt", "Hello docs".getBytes());
        fs.createFile(admin, "/docs", "report.pdf", 56000, "pdf", null);
        fs.createFile(admin, "/media", "photo.jpg", 250_000, "jpg", null);

        // Bob створює свою папку і файл
        fs.mkdir(bob, "/docs/personal");
        fs.createFile(bob, "/docs/personal", "notes.txt", 300, "txt", "todo".getBytes());

        // Пошук і фільтрація
        System.out.println("\n--- Пошук 're' у /docs ---");
        fs.search(admin, "/docs", "re").forEach(System.out::println);

        System.out.println("\n--- Файли з розширенням 'txt' у /docs ---");
        fs.filterByExtension(admin, "/docs", "txt").forEach(System.out::println);

        // Копіювання та переміщення
        System.out.println("\n--- Копіювання /docs/personal -> /media ---");
        fs.copy(admin, "/docs/personal", "/media");

        System.out.println("--- Переміщення /media/photo.jpg -> /docs ---");
        fs.move(admin, "/media/photo.jpg", "/docs");

        // Видалення (спроба видалити непорожній каталог)
        try {
            fs.deleteDir(admin, "/media/personal");
        } catch (InvalidOperationException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        // Видалення файлу
        fs.deleteFile(admin, "/docs/readme.txt");

        // Перевірка прав: Bob не має доступу до /media
        try {
            fs.createFile(bob, "/media", "clip.mp4", 1_000_000, "mp4", null);
        } catch (PermissionDeniedException ex) {
            System.out.println("[DENIED] " + ex.getMessage());
        }

        // Фінальний стан
        System.out.println("\n=== Підсумок ===");
        System.out.println(docs);
        System.out.println(media);
        System.out.println("Вміст /docs:");
        docs.getFiles().forEach(f -> System.out.println("  " + f));
        docs.getSubdirs().forEach(d -> System.out.println("  " + d));
        System.out.println("Вміст /media:");
        media.getFiles().forEach(f -> System.out.println("  " + f));
        media.getSubdirs().forEach(d -> System.out.println("  " + d));
    }
}