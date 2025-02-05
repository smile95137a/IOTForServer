package src.main.java.com.frontend.runner;//package com.mo.app.runner;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Random;
//import java.util.Set;
//import java.util.stream.IntStream;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import com.mo.app.entity.job.ScheduledJob;
//import com.mo.app.entity.layout.Layout;
//import com.mo.app.entity.layout.LayoutArea;
//import com.mo.app.entity.user.Role;
//import com.mo.app.entity.user.User;
//import com.mo.app.enums.LayoutSize;
//import com.mo.app.enums.RoleName;
//import com.mo.app.repo.LayoutAreaRepository;
//import com.mo.app.repo.LayoutRepository;
//import com.mo.app.repo.MenuItemRepository;
//import com.mo.app.repo.RoleRepository;
//import com.mo.app.repo.ScheduledJobRepository;
//import com.mo.app.repo.UserRepository;
//import com.mo.app.service.MailService;
//import com.mo.app.utils.RandomUtils;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DatabaseInitializer2 implements CommandLineRunner {
//
//	private final PasswordEncoder passwordEncoder;
//
//	private final RoleRepository roleRepository;
//
//	private final UserRepository userRepository;
//
//	private final LayoutRepository layoutRepository;
//
//	private final LayoutAreaRepository layoutAreaRepository;
//
//	private final MenuItemRepository menuItemRepository;
//	private final MailService mailService;
//	
//	private final ScheduledJobRepository scheduledJobRepository;
//
//	private static final Random RANDOM = new Random();
//
//	@Override
//	public void run(String... args) throws IOException {
//		this.createLayout();
//		if (!userRepository.findAll().isEmpty()) {
//			return;
//		} 
//		createRoles();
//		createAdminUser();
//
//		log.info("Database initialized");
//	}
//
//	private void createRoles() {
//		if (!roleRepository.existsByName(RoleName.ROLE_ADMIN)) {
//			Role adminRole = new Role();
//			adminRole.setName(RoleName.ROLE_ADMIN);
//			roleRepository.save(adminRole);
//		}
//
//		if (!roleRepository.existsByName(RoleName.ROLE_USER)) {
//			Role userRole = new Role();
//			userRole.setName(RoleName.ROLE_USER);
//			roleRepository.save(userRole);
//		}
//	}
//
//	private void createAdminUser() {
//		Set<Role> adminRoles = new HashSet<>();
//		Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
//				.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not found."));
//		adminRoles.add(adminRole);
//
//		User admin = new User();
//		admin.setUid(RandomUtils.genRandom(32, false));
//		admin.setUsername("admin");
//		admin.setPassword(passwordEncoder.encode("123456"));
//		admin.setName("ADMIN");
//		admin.setEmail("admin@gmail.com");
//		admin.setRoles(adminRoles);
//		admin.setCreateTime(LocalDateTime.now());
//		admin.setUpdateTime(LocalDateTime.now());
//		admin.setLastActiveTime(LocalDateTime.now());
//
//		userRepository.save(admin);
//	}
//
//	private void createStandardUsers(int numberOfUsers) {
//		Set<Role> userRoles = new HashSet<>();
//		Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
//				.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not found."));
//		userRoles.add(userRole);
//
//		IntStream.range(0, numberOfUsers).forEach(i -> {
//			User user = new User();
//			user.setUid(RandomUtils.genRandom(32, false));
//			user.setUsername("user" + i);
//			user.setPassword(passwordEncoder.encode("123456"));
//			user.setName(RandomUtils.genRandom(32, false));
//			user.setEmail(RandomUtils.genRandom(32, false) + "@example.com");
//			user.setRoles(userRoles);
//			user.setCreateTime(LocalDateTime.now());
//			user.setUpdateTime(LocalDateTime.now());
//			user.setLastActiveTime(LocalDateTime.now());
//
//			userRepository.save(user);
//		});
//	}
//
//	private void createLayout() {
//
//		LayoutArea area1 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(833).height(843).build();
//		LayoutArea area2 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(834).y(0).width(833).height(843)
//				.build();
//		LayoutArea area3 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1668).y(0).width(832).height(843)
//				.build();
//		LayoutArea area4 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(843).width(833).height(843)
//				.build();
//		LayoutArea area5 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(834).y(843).width(833).height(843)
//				.build();
//		LayoutArea area6 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1668).y(843).width(832).height(843)
//				.build();
//
//		List<LayoutArea> areas = Arrays.asList(area1, area2, area3, area4, area5, area6);
//
//		Layout layout = Layout.builder().name("版型1").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL).areas(areas)
//				.createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout);
//
//		LayoutArea halfArea1 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(1250).height(421)
//				.build();
//		LayoutArea halfArea2 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1251).y(0).width(1240).height(421)
//				.build();
//		LayoutArea halfArea3 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(421).width(2500).height(422)
//				.build();
//
//		List<LayoutArea> halfAreas = Arrays.asList(halfArea1, halfArea2, halfArea3);
//
//		Layout halfLayout = Layout.builder().name("版型2").uid(RandomUtils.genRandom(32)).size(LayoutSize.HALF)
//				.areas(halfAreas).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(halfLayout);
//
//		//
//		LayoutArea area21 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(1249).height(843)
//				.build();
//		LayoutArea area22 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(0).width(1250).height(843)
//				.build();
//		LayoutArea area23 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(843).width(1249).height(843)
//				.build();
//		LayoutArea area24 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1250).y(843).width(1250).height(843)
//				.build();
//
//		List<LayoutArea> areas22 = Arrays.asList(area21, area22, area23, area24);
//
//		Layout layout22 = Layout.builder().name("版型2").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas22).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout22);
//
//		LayoutArea area31 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(2500).height(843)
//				.build();
//		LayoutArea area32 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(844).width(833).height(843)
//				.build();
//		LayoutArea area33 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(834).y(844).width(833).height(843)
//				.build();
//		LayoutArea area34 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1668).y(844).width(833).height(843)
//				.build();
//
//		List<LayoutArea> areas33 = Arrays.asList(area31, area32, area33, area34);
//
//		Layout layout33 = Layout.builder().name("版型3").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas33).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout33);
//
//		LayoutArea area41 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(0).width(625).height(843)
//				.build();
//		LayoutArea area42 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(626).y(0).width(625).height(843)
//				.build();
//		LayoutArea area43 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1252).y(0).width(625).height(843)
//				.build();
//		LayoutArea area44 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1878).y(0).width(625).height(843)
//				.build();
//		LayoutArea area45 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(0).y(844).width(625).height(843)
//				.build();
//		LayoutArea area46 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(626).y(844).width(1250).height(843)
//				.build();
//		LayoutArea area47 = LayoutArea.builder().uid(RandomUtils.genRandom(32)).x(1872).y(844).width(623).height(843)
//				.build();
//
//		List<LayoutArea> areas44 = Arrays.asList(area41, area42, area43, area44, area45, area46, area47);
//
//		Layout layout44 = Layout.builder().name("版型4").uid(RandomUtils.genRandom(32)).size(LayoutSize.FULL)
//				.areas(areas44).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now()).build();
//
//		layoutRepository.save(layout44);
//	}
//	
//	
//	   private void createScheduledJobs() {
//	       ScheduledJob job1 = ScheduledJob.builder()
//	               .jobName("TestJob")
//	               .cronExpression("0/5 * * * * ?")
//	               .beanClass("com.mo.app.job..TestJob")
//	               .status("NONE")
//	               .jobGroup("default")
//	               .jobDataMap("{\"username\":\"zhangsan\", \"age\":18}")
//	               .createTime(LocalDateTime.now())
//	               .createUserId(1L)
//	               .updateTime(LocalDateTime.now())
//	               .updateUserId(1L)
//	               .lastActiveTime(LocalDateTime.now())
//	               .remarks("测试定时任务1")
//	               .build();
//
//	       ScheduledJob job2 = ScheduledJob.builder()
//	               .jobName("Test2Job")
//	               .cronExpression("0 * * * * ?")
//	               .beanClass("com.mo.app.job.Test2Job")
//	               .status("NONE")
//	               .jobGroup("default")
//	               .jobDataMap("{\"username\":\"lisi\", \"age\":20}")
//	               .createTime(LocalDateTime.now())
//	               .createUserId(1L)
//	               .updateTime(LocalDateTime.now())
//	               .updateUserId(1L)
//	               .lastActiveTime(LocalDateTime.now())
//	               .remarks("测试定时任务2")
//	               .build();
//
//	       scheduledJobRepository.save(job1);
//	       scheduledJobRepository.save(job2);
//
//	       log.info("Scheduled jobs created");
//	   }
//
//}
