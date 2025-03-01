package app.library.management.core.repository.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileStorage {

    private final ObjectMapper objectMapper;
    private final File file;
    private final ReentrantReadWriteLock lock;

    public FileStorage(String filePath) {
        this.file = new File(filePath);
        this.lock = new ReentrantReadWriteLock();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    /**
     * 파일 읽기
     *
     * @return
     */
    public List<BookVO> readFile() {
        List<BookVO> list = new ArrayList<>();
        lock.readLock().lock();
        try {
            list = objectMapper.readValue(file, new TypeReference<List<BookVO>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return list;
    }

    /**
     * 파일에 저장
     *
     * @param bookVO
     */
    public void saveFile(BookVO bookVO) {
        lock.writeLock().lock();
        try {
            List<BookVO> list = objectMapper.readValue(file, new TypeReference<List<BookVO>>(){});
            list.add(bookVO);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 파일에서 삭제
     *
     * @param bookVO
     */
    public void deleteFile(BookVO bookVO) {
        lock.writeLock().lock();
        try {
            List<BookVO> list = objectMapper.readValue(file, new TypeReference<List<BookVO>>(){});
            list.remove(bookVO);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 파일에 갱신
     *
     * @param bookVO
     */
    public void updateFile(BookVO bookVO) {
        lock.writeLock().lock();
        try {
            List<BookVO> list = objectMapper.readValue(file, new TypeReference<List<BookVO>>(){});
            Optional<BookVO> optional = list.stream().filter(b -> b.getId() == bookVO.getId())
                    .findAny();
            if (optional.isPresent()) {
                BookVO vo = optional.get();
                vo.setStatus(bookVO.getStatus());
                vo.setLastModifiedTime(bookVO.getLastModifiedTime());
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
